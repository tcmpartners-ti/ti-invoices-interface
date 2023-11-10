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
import com.tcmp.tiapi.titoapigee.exception.UnrecoverableApiGeeRequestException;
import com.tcmp.tiapi.titoapigee.operationalgateway.OperationalGatewayService;
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
    from(uriFrom).routeId("invoiceFinanceResultFlow")
      .log("Started invoice settle flow.")
      .process().body(AckServiceRequest.class, this::startInvoiceSettlementFlow)
      .log("Completed invoice settle flow.")
      .end();
  }

  private void startInvoiceSettlementFlow(AckServiceRequest<CreateDueInvoiceEventMessage> serviceResponse) {
    if (serviceResponse == null) throw new UnrecoverableApiGeeRequestException("Message with no body received.");
    CreateDueInvoiceEventMessage message = serviceResponse.getBody();
    BigDecimal paymentAmount = MonetaryAmountUtils.convertCentsToDollars(new BigDecimal(message.getPaymentAmount()));

    Customer buyer = invoiceSettlementService.findCustomerByMnemonic(message.getBuyerIdentifier());
    Customer seller = invoiceSettlementService.findCustomerByMnemonic(message.getSellerIdentifier());
    ProgramExtension programExtension = invoiceSettlementService.findProgrammeExtensionByIdOrDefault(message.getProgramme());
    InvoiceMaster invoice = invoiceSettlementService.findInvoiceByMasterRef(message.getMasterRef());
    ProductMasterExtension invoiceExtension = invoiceSettlementService.findProductMasterExtensionByMasterId(invoice.getId());
    EncodedAccountParser buyerAccountParser = new EncodedAccountParser(invoiceExtension.getFinanceAccount());

    boolean hasExtraFinancingDays = programExtension.getExtraFinancingDays() > 0;
    boolean hasBeenFinanced = invoiceSettlementService.invoiceHasLinkedFinanceEvent(invoice);

    if (hasExtraFinancingDays) {
      if (hasBeenFinanced) {
        InvoiceEmailInfo creditedInvoiceInfo = invoiceSettlementService.buildInvoiceSettlementEmailInfo(
          message, buyer, InvoiceEmailEvent.CREDITED, paymentAmount);
        operationalGatewayService.sendNotificationRequest(creditedInvoiceInfo);
        return; // In this case, end flow, we only notify credit to buyer
      }

      InvoiceEmailInfo settledInvoiceInfo = invoiceSettlementService.buildInvoiceSettlementEmailInfo(
        message, seller, InvoiceEmailEvent.SETTLED, paymentAmount);
      operationalGatewayService.sendNotificationRequest(settledInvoiceInfo);
    }

    DistributorCreditResponse creditResponse = corporateLoanService.createCredit(
      invoiceSettlementService.buildDistributorCreditRequest(message, buyer, programExtension, buyerAccountParser));
    Error creditResponseError = creditResponse.data().error();

    boolean hasBeenCredited = creditResponseError != null && creditResponseError.hasNoError();
    if (!hasBeenCredited) {
      log.error("Could not continue settlement flow, credit could not be created.");
      return;
    }

    if (hasExtraFinancingDays) { // We don't care if invoice has been financed or not
      InvoiceEmailInfo creditedEmailInfo = invoiceSettlementService.buildInvoiceSettlementEmailInfo(
        message, buyer, InvoiceEmailEvent.CREDITED, paymentAmount);
      operationalGatewayService.sendNotificationRequest(creditedEmailInfo);
    }

    if (hasBeenFinanced) return;

    boolean isBglToSellerTransactionOk = transferPaymentAmountToSeller(message, buyer);
    if (hasExtraFinancingDays && isBglToSellerTransactionOk) {
      InvoiceEmailInfo processedInvoiceInfo = invoiceSettlementService.buildInvoiceSettlementEmailInfo(
        message, seller, InvoiceEmailEvent.PROCESSED, paymentAmount);
      operationalGatewayService.sendNotificationRequest(processedInvoiceInfo);
    }
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
