package com.tcmp.tiapi.invoice.route;

import com.tcmp.tiapi.customer.model.Account;
import com.tcmp.tiapi.customer.model.Customer;
import com.tcmp.tiapi.invoice.dto.ti.CreateDueInvoiceEventMessage;
import com.tcmp.tiapi.invoice.dto.ti.financeack.FinanceAckMessage;
import com.tcmp.tiapi.invoice.model.InvoiceMaster;
import com.tcmp.tiapi.invoice.model.ProductMasterExtension;
import com.tcmp.tiapi.invoice.service.InvoiceFinancingService;
import com.tcmp.tiapi.invoice.service.InvoiceSettlementService;
import com.tcmp.tiapi.invoice.util.EncodedAccountParser;
import com.tcmp.tiapi.messaging.model.TINamespace;
import com.tcmp.tiapi.messaging.model.TIOperation;
import com.tcmp.tiapi.messaging.model.requests.AckServiceRequest;
import com.tcmp.tiapi.program.model.ProgramExtension;
import com.tcmp.tiapi.shared.utils.MonetaryAmountUtils;
import com.tcmp.tiapi.titoapigee.corporateloan.CorporateLoanService;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.response.DistributorCreditResponse;
import com.tcmp.tiapi.titoapigee.exception.UnrecoverableApiGeeRequestException;
import com.tcmp.tiapi.titoapigee.operationalgateway.OperationalGatewayService;
import com.tcmp.tiapi.titoapigee.operationalgateway.model.InvoiceEmailEvent;
import com.tcmp.tiapi.titoapigee.paymentexecution.PaymentExecutionService;
import com.tcmp.tiapi.titoapigee.paymentexecution.dto.response.BusinessAccountTransfersResponse;
import com.tcmp.tiapi.titoapigee.paymentexecution.exception.PaymentExecutionException;
import lombok.RequiredArgsConstructor;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.ValueBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.support.builder.Namespaces;

import java.math.BigDecimal;

@RequiredArgsConstructor

public class InvoiceAckEventListenerRouteBuilder extends RouteBuilder {
  private final JaxbDataFormat jaxbDataFormat;

  private final InvoiceFinancingService invoiceFinancingService;
  private final InvoiceSettlementService invoiceSettlementService;
  private final CorporateLoanService corporateLoanService;
  private final PaymentExecutionService paymentExecutionService;
  private final OperationalGatewayService operationalGatewayService;

  private final String uriFrom;

  @Override
  public void configure() {
    Namespaces ns = new Namespaces("ns2", TINamespace.CONTROL);
    ValueBuilder operationXpath = xpath("//ns2:ServiceRequest/ns2:RequestHeader/ns2:Operation", String.class, ns);

    // For now, don't handle errors

    from(uriFrom).routeId("invoiceAckEventResult")
      .unmarshal(jaxbDataFormat)
      .choice()
        .when(operationXpath.isEqualTo(TIOperation.DUE_INVOICE_VALUE))
          .log("Started invoice settlement flow.")
          .process().body(AckServiceRequest.class, this::startInvoiceSettlementFlow)
          .log("Invoice settlement flow completed successfully.")
        .endChoice()

        .when(operationXpath.isEqualTo(TIOperation.FINANCE_ACK_INVOICE_VALUE))
          .log("Started invoice financing flow.")
          .process().body(AckServiceRequest.class, this::startInvoiceFinancingFlow)
          .log("Invoice financing flow completed successfully.")
        .endChoice()

        .otherwise()
          .log(LoggingLevel.ERROR, "Unknown Trade Innovation operation.")
          .to("log:body")
        .endChoice()

      .endChoice()
      .end();
  }

  private void startInvoiceSettlementFlow(AckServiceRequest<CreateDueInvoiceEventMessage> serviceResponse) {
    if (serviceResponse == null) throw new UnrecoverableApiGeeRequestException("Message with no body received.");

    CreateDueInvoiceEventMessage message = serviceResponse.getBody();
    Customer buyer = invoiceSettlementService.findCustomerByMnemonic(message.getBuyerIdentifier());
    Customer seller = invoiceSettlementService.findCustomerByMnemonic(message.getSellerIdentifier());
    ProgramExtension programmeExtension = invoiceSettlementService.findByProgrammeIdOrDefault(message.getProgramme());
    InvoiceMaster invoice = invoiceSettlementService.findInvoiceByMasterRef(message.getMasterRef());
    ProductMasterExtension invoiceExtension = invoiceSettlementService.findInvoiceExtension(invoice);
    EncodedAccountParser buyerAccountParser = new EncodedAccountParser(invoiceExtension.getFinanceAccount());

    DistributorCreditResponse settlementDistributorCreditResponse = corporateLoanService.createCredit(
      invoiceSettlementService.buildSettlementDistributorCreditRequest(message, buyer, programmeExtension, buyerAccountParser));

    // If we reach this point credit is created.
    boolean programmeHasExtraFinancingDays = programmeExtension.getExtraFinancingDays() > 0;
    InvoiceEmailEvent event = programmeHasExtraFinancingDays ? InvoiceEmailEvent.CREDITED : InvoiceEmailEvent.PROCESSED;
    BigDecimal paidAmountInDollars = MonetaryAmountUtils.convertCentsToDollars(new BigDecimal(message.getPaymentAmount()));
    operationalGatewayService.sendNotificationRequest(
      invoiceSettlementService.buildInvoiceSettlementEmailInfo(message, buyer, event, paidAmountInDollars));

    boolean hasPrepayment = invoiceSettlementService.invoiceHasLinkedPrepaymentEvent(invoice);
    if (hasPrepayment) {
      Account sellerAccount = invoiceSettlementService.findCustomerAccountByMnemonic(message.getSellerIdentifier());
      EncodedAccountParser sellerAccountParser = new EncodedAccountParser(sellerAccount.getExternalAccountNumber());

      BusinessAccountTransfersResponse bglToSellerTransactionResponse = paymentExecutionService.makeTransactionRequest(
        invoiceSettlementService.buildBglToSellerTransaction(
          message, settlementDistributorCreditResponse, seller, sellerAccountParser));

      boolean transactionExecutedSuccessfully = "OK".equals(bglToSellerTransactionResponse.data().status());
      if (!transactionExecutedSuccessfully) {
        log.error("Invoice settlement process failed.");
        return;
      }

      operationalGatewayService.sendNotificationRequest(
        invoiceSettlementService.buildInvoiceSettlementEmailInfo(
          message, seller, InvoiceEmailEvent.PROCESSED, paidAmountInDollars));
      return;
    }

    // Invoice has no prepayment event linked
    operationalGatewayService.sendNotificationRequest(
      invoiceSettlementService.buildInvoiceSettlementEmailInfo(
        message, seller, InvoiceEmailEvent.SETTLED, paidAmountInDollars));
  }

  private void startInvoiceFinancingFlow(AckServiceRequest<FinanceAckMessage> serviceRequest) {
    if (serviceRequest == null) throw new UnrecoverableApiGeeRequestException("Message with no body received.");
    FinanceAckMessage invoicePrepaymentMessage = serviceRequest.getBody();
    String invoiceReference = invoicePrepaymentMessage.getInvoiceArray().get(0).getInvoiceReference();

    Customer buyer = invoiceFinancingService.findCustomerByMnemonic(invoicePrepaymentMessage.getBuyerIdentifier());
    Customer seller = invoiceFinancingService.findCustomerByMnemonic(invoicePrepaymentMessage.getSellerIdentifier());
    ProductMasterExtension invoiceExtension = invoiceFinancingService.findFinanceAccountFromInvoiceData(invoiceReference);
    Account sellerAccount = invoiceFinancingService.findCustomerAccount(invoicePrepaymentMessage.getSellerIdentifier());

    EncodedAccountParser buyerAccountParser = new EncodedAccountParser(invoiceExtension.getFinanceAccount());
    EncodedAccountParser sellerAccountParser = new EncodedAccountParser(sellerAccount.getExternalAccountNumber());

    operationalGatewayService.sendNotificationRequest(
      invoiceFinancingService.buildInvoiceFinancingEmailInfo(invoicePrepaymentMessage, seller, InvoiceEmailEvent.FINANCED));

    DistributorCreditResponse creditResponse = corporateLoanService.createCredit(
      invoiceFinancingService.buildDistributorCreditRequest(invoicePrepaymentMessage, buyer, buyerAccountParser));

    boolean buyerToSellerTransactionSuccessful = transferCreditAmountFromBuyerToSeller(
      creditResponse, invoicePrepaymentMessage, buyerAccountParser, sellerAccountParser);

    if (!buyerToSellerTransactionSuccessful) {
      log.error("Invoice prepayment flow failed.");
      return;
    }

    operationalGatewayService.sendNotificationRequest(
      invoiceFinancingService.buildInvoiceFinancingEmailInfo(invoicePrepaymentMessage, seller, InvoiceEmailEvent.PROCESSED));
  }

  private boolean transferCreditAmountFromBuyerToSeller(
    DistributorCreditResponse distributorCreditResponse,
    FinanceAckMessage invoicePrepaymentAckMessage,
    EncodedAccountParser buyerAccount,
    EncodedAccountParser sellerAccount
  ) {
    try {
      BusinessAccountTransfersResponse buyerToBglResponse = paymentExecutionService.makeTransactionRequest(
        invoiceFinancingService.buildBuyerToBglTransactionRequest(
          distributorCreditResponse, invoicePrepaymentAckMessage, buyerAccount));

      BusinessAccountTransfersResponse bglToSellerResponse = paymentExecutionService.makeTransactionRequest(
        invoiceFinancingService.buildBglToSellerTransactionRequest(
          distributorCreditResponse, invoicePrepaymentAckMessage, sellerAccount));

      if (buyerToBglResponse.data() == null || bglToSellerResponse.data() == null) {
        return false;
      }

      String buyerToBglStatus = buyerToBglResponse.data().status();
      String bglToSellerStatus = bglToSellerResponse.data().status();

      log.info("[Anchor -> Bgl]: {} [Bgl -> Seller]: {}", buyerToBglStatus, bglToSellerStatus);

      return "OK".equals(buyerToBglStatus) && "OK".equals(bglToSellerStatus);
    } catch (PaymentExecutionException e) {
      return false;
    }
  }
}
