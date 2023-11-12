package com.tcmp.tiapi.invoice.route;

import com.tcmp.tiapi.customer.model.Account;
import com.tcmp.tiapi.customer.model.Customer;
import com.tcmp.tiapi.invoice.dto.ti.CreateDueInvoiceEventMessage;
import com.tcmp.tiapi.invoice.model.InvoiceMaster;
import com.tcmp.tiapi.invoice.model.ProductMasterExtension;
import com.tcmp.tiapi.invoice.service.InvoiceSettlementService;
import com.tcmp.tiapi.invoice.util.EncodedAccountParser;
import com.tcmp.tiapi.messaging.model.requests.AckServiceRequest;
import com.tcmp.tiapi.program.model.ProgramExtension;
import com.tcmp.tiapi.shared.utils.MonetaryAmountUtils;
import com.tcmp.tiapi.titoapigee.corporateloan.CorporateLoanService;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.response.DistributorCreditResponse;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.response.Error;
import com.tcmp.tiapi.titoapigee.corporateloan.exception.CreditCreationException;
import com.tcmp.tiapi.titoapigee.exception.UnrecoverableApiGeeRequestException;
import com.tcmp.tiapi.titoapigee.operationalgateway.OperationalGatewayService;
import com.tcmp.tiapi.titoapigee.operationalgateway.exception.OperationalGatewayException;
import com.tcmp.tiapi.titoapigee.operationalgateway.model.InvoiceEmailEvent;
import com.tcmp.tiapi.titoapigee.operationalgateway.model.InvoiceEmailInfo;
import com.tcmp.tiapi.titoapigee.paymentexecution.PaymentExecutionService;
import com.tcmp.tiapi.titoapigee.paymentexecution.dto.response.BusinessAccountTransfersResponse;
import com.tcmp.tiapi.titoapigee.paymentexecution.exception.PaymentExecutionException;
import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;

import java.math.BigDecimal;

@RequiredArgsConstructor
public class InvoiceSettleResultFlowBuilder extends RouteBuilder {
  private final InvoiceSettlementService invoiceSettlementService;
  private final CorporateLoanService corporateLoanService;
  private final PaymentExecutionService paymentExecutionService;
  private final OperationalGatewayService operationalGatewayService;

  private final String uriFrom;

  @Override
  public void configure() {
    onException(OperationalGatewayException.class, CreditCreationException.class, PaymentExecutionException.class)
      .handled(true)
      .to("log:myLogger?level=ERROR&showCaughtException=true&showStackTrace=false")
      .end();

    from(uriFrom).routeId("invoiceSettleResultFlow")
      .log("Started invoice settle flow.")
      .process().body(AckServiceRequest.class, this::startInvoiceSettlementFlow)
      .log("Completed invoice settle flow.")
      .end();
  }

  private void startInvoiceSettlementFlow(AckServiceRequest<CreateDueInvoiceEventMessage> serviceResponse) {
    if (serviceResponse == null) throw new UnrecoverableApiGeeRequestException("Message with no body received.");
    CreateDueInvoiceEventMessage message = serviceResponse.getBody();
    BigDecimal paymentAmountInCents = new BigDecimal(message.getPaymentAmount());
    BigDecimal paymentAmount = MonetaryAmountUtils.convertCentsToDollars(paymentAmountInCents);

    Customer buyer = invoiceSettlementService.findCustomerByMnemonic(message.getBuyerIdentifier());
    Customer seller = invoiceSettlementService.findCustomerByMnemonic(message.getSellerIdentifier());
    ProgramExtension programExtension = invoiceSettlementService.findProgrammeExtensionByIdOrDefault(message.getProgramme());
    InvoiceMaster invoice = invoiceSettlementService.findInvoiceByMasterRef(message.getMasterRef());
    ProductMasterExtension invoiceExtension = invoiceSettlementService.findProductMasterExtensionByMasterId(invoice.getId());
    EncodedAccountParser buyerAccountParser = new EncodedAccountParser(invoiceExtension.getFinanceAccount());

    boolean hasExtraFinancingDays = programExtension.getExtraFinancingDays() > 0;
    boolean hasBeenFinanced = invoiceSettlementService.invoiceHasLinkedFinanceEvent(invoice);

    // For Mvp, we ignore invoices with no extra financing days.
    if (!hasExtraFinancingDays) {
      log.info("Programe has no extra financing days, flow ended.");
      return;
    }

    // Second settlement case: end flow, we only notify credit to buyer.
    if (hasBeenFinanced) {
      InvoiceEmailInfo creditedInvoiceInfo = invoiceSettlementService.buildInvoiceSettlementEmailInfo(
        message, buyer, InvoiceEmailEvent.CREDITED, paymentAmount);
      operationalGatewayService.sendNotificationRequest(creditedInvoiceInfo);
      return;
    }

    InvoiceEmailInfo settledInvoiceInfo = invoiceSettlementService.buildInvoiceSettlementEmailInfo(
      message, seller, InvoiceEmailEvent.SETTLED, paymentAmount);
    operationalGatewayService.sendNotificationRequest(settledInvoiceInfo);

    DistributorCreditResponse creditResponse = corporateLoanService.createCredit(
      invoiceSettlementService.buildDistributorCreditRequest(message, buyer, programExtension, buyerAccountParser));
    Error creditResponseError = creditResponse.data().error();

    boolean hasBeenCredited = creditResponseError != null && creditResponseError.hasNoError();
    if (!hasBeenCredited) {
      log.error("Could not complete settlement flow, credit creation failed.");
      return;
    }

    // We don't care if invoice has been financed or not
    InvoiceEmailInfo creditedEmailInfo = invoiceSettlementService.buildInvoiceSettlementEmailInfo(
      message, buyer, InvoiceEmailEvent.CREDITED, paymentAmount);
    operationalGatewayService.sendNotificationRequest(creditedEmailInfo);

    boolean isBglToSellerTransactionOk = transferPaymentAmountToSeller(message, buyer);
    if (!isBglToSellerTransactionOk) {
      log.error("Could not complete settlement flow, bgl to seller transaction failed.");
    }

    InvoiceEmailInfo processedInvoiceInfo = invoiceSettlementService.buildInvoiceSettlementEmailInfo(
      message, seller, InvoiceEmailEvent.PROCESSED, paymentAmount);
    operationalGatewayService.sendNotificationRequest(processedInvoiceInfo);
  }

  private boolean transferPaymentAmountToSeller(CreateDueInvoiceEventMessage message, Customer buyer) {
    Account sellerAccount = invoiceSettlementService.findAccountByCustomerMnemonic(message.getSellerIdentifier());
    EncodedAccountParser sellerAccountParser = new EncodedAccountParser(sellerAccount.getExternalAccountNumber());

    try {
      BusinessAccountTransfersResponse bglToSellerTransactionResponse = paymentExecutionService.makeTransactionRequest(
        invoiceSettlementService.buildBglToSellerTransaction(message, buyer, sellerAccountParser));

      return "OK".equals(bglToSellerTransactionResponse.data().status());
    } catch (PaymentExecutionException e) {
      return false;
    }
  }
}
