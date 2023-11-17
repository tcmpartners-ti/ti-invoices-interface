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
import com.tcmp.tiapi.titoapigee.businessbanking.BusinessBankingService;
import com.tcmp.tiapi.titoapigee.businessbanking.dto.request.OperationalGatewayRequestPayload;
import com.tcmp.tiapi.titoapigee.businessbanking.dto.request.PayloadDetails;
import com.tcmp.tiapi.titoapigee.businessbanking.dto.request.PayloadInvoice;
import com.tcmp.tiapi.titoapigee.businessbanking.dto.request.PayloadStatus;
import com.tcmp.tiapi.titoapigee.businessbanking.model.OperationalGatewayProcessCode;
import com.tcmp.tiapi.titoapigee.corporateloan.CorporateLoanService;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.response.DistributorCreditResponse;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.response.Error;
import com.tcmp.tiapi.titoapigee.corporateloan.exception.CreditCreationException;
import com.tcmp.tiapi.titoapigee.exception.UnrecoverableApiGeeRequestException;
import com.tcmp.tiapi.titoapigee.operationalgateway.OperationalGatewayService;
import com.tcmp.tiapi.titoapigee.operationalgateway.model.InvoiceEmailEvent;
import com.tcmp.tiapi.titoapigee.operationalgateway.model.InvoiceEmailInfo;
import com.tcmp.tiapi.titoapigee.paymentexecution.PaymentExecutionService;
import com.tcmp.tiapi.titoapigee.paymentexecution.dto.response.BusinessAccountTransfersResponse;
import com.tcmp.tiapi.titoapigee.paymentexecution.exception.PaymentExecutionException;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;

import java.math.BigDecimal;
import java.util.List;

@RequiredArgsConstructor
public class InvoiceSettleResultFlowBuilder extends RouteBuilder {
  private final InvoiceSettlementService invoiceSettlementService;
  private final CorporateLoanService corporateLoanService;
  private final PaymentExecutionService paymentExecutionService;
  private final OperationalGatewayService operationalGatewayService;
  private final BusinessBankingService businessBankingService;

  private final String uriFrom;

  @Override
  public void configure() {
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
        InvoiceEmailEvent.CREDITED, message, buyer, paymentAmount);
      operationalGatewayService.sendNotificationRequest(creditedInvoiceInfo);
      return;
    }

    InvoiceEmailInfo settledInvoiceInfo = invoiceSettlementService.buildInvoiceSettlementEmailInfo(
      InvoiceEmailEvent.SETTLED, message, seller, paymentAmount);
    operationalGatewayService.sendNotificationRequest(settledInvoiceInfo);

    try {
      log.info("Started credit creation.");
      DistributorCreditResponse creditResponse = corporateLoanService.createCredit(
        invoiceSettlementService.buildDistributorCreditRequest(message, buyer, programExtension, buyerAccountParser));
      Error creditResponseError = creditResponse.data().error();

      boolean hasBeenCredited = creditResponseError != null && creditResponseError.hasNoError();
      if (!hasBeenCredited) {
        String creditCreationError = creditResponseError != null
          ? creditResponseError.message()
          : "Credit creation failed.";
        log.error(creditCreationError);
        notifySettlementStatus(PayloadStatus.FAILED, message, invoice, creditCreationError);

        return;
      }

      // We don't care if invoice has been financed or not
      InvoiceEmailInfo creditedEmailInfo = invoiceSettlementService.buildInvoiceSettlementEmailInfo(
        InvoiceEmailEvent.CREDITED, message, buyer, paymentAmount);
      operationalGatewayService.sendNotificationRequest(creditedEmailInfo);

      log.info("Started buyer to seller transaction.");
      boolean isBuyerToSellerTransactionOk = transferPaymentAmountFromBuyerToSeller(message, buyer, seller, buyerAccountParser);
      if (!isBuyerToSellerTransactionOk) {
        String transferError = "Could not transfer invoice payment amount from buyer to seller.";
        log.error(transferError);
        notifySettlementStatus(PayloadStatus.FAILED, message, invoice, transferError);

        return;
      }

      InvoiceEmailInfo processedInvoiceInfo = invoiceSettlementService.buildInvoiceSettlementEmailInfo(
        InvoiceEmailEvent.PROCESSED, message, seller, paymentAmount);
      operationalGatewayService.sendNotificationRequest(processedInvoiceInfo);

      notifySettlementStatus(PayloadStatus.SUCCEEDED, message, invoice, null);
    } catch (CreditCreationException e) {
      notifySettlementStatus(PayloadStatus.FAILED, message, invoice, e.getMessage());
    } catch (PaymentExecutionException e) {
      notifySettlementStatus(PayloadStatus.FAILED, message, invoice, e.getTransferResponseError().title());
    }
  }

  private boolean transferPaymentAmountFromBuyerToSeller(
    CreateDueInvoiceEventMessage message,
    Customer buyer,
    Customer seller,
    EncodedAccountParser buyerAccountParser
  ) throws PaymentExecutionException {
    Account sellerAccount = invoiceSettlementService.findAccountByCustomerMnemonic(message.getSellerIdentifier());
    EncodedAccountParser sellerAccountParser = new EncodedAccountParser(sellerAccount.getExternalAccountNumber());

    BusinessAccountTransfersResponse buyerToBglTransactionResponse = paymentExecutionService.makeTransactionRequest(
      invoiceSettlementService.buildBuyerToBglTransactionRequest(message, seller, buyerAccountParser));

    BusinessAccountTransfersResponse bglToSellerTransactionResponse = paymentExecutionService.makeTransactionRequest(
      invoiceSettlementService.buildBglToSellerTransaction(message, buyer, sellerAccountParser));

    if (buyerToBglTransactionResponse == null || bglToSellerTransactionResponse == null) return false;
    return buyerToBglTransactionResponse.isOk() && bglToSellerTransactionResponse.isOk();
  }

  private void notifySettlementStatus(
    PayloadStatus status,
    CreateDueInvoiceEventMessage message,
    InvoiceMaster invoice,
    @Nullable String error
  ) {
    List<String> errors = error == null ? null : List.of(error);

    OperationalGatewayRequestPayload payload = OperationalGatewayRequestPayload.builder()
      .status(status.getValue())
      .invoice(PayloadInvoice.builder()
        .batchId(invoice.getBatchId().trim())
        .reference(message.getInvoiceNumber())
        .sellerMnemonic(message.getSellerIdentifier())
        .build())
      .details(new PayloadDetails(errors, null, null))
      .build();

    businessBankingService.notifyEvent(OperationalGatewayProcessCode.INVOICE_SETTLEMENT, payload);
  }
}
