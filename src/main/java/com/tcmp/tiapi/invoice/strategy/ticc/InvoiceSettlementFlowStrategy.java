package com.tcmp.tiapi.invoice.strategy.ticc;

import com.tcmp.tiapi.customer.model.Account;
import com.tcmp.tiapi.customer.model.Customer;
import com.tcmp.tiapi.invoice.dto.ti.CreateDueInvoiceEventMessage;
import com.tcmp.tiapi.invoice.model.InvoiceMaster;
import com.tcmp.tiapi.invoice.model.ProductMasterExtension;
import com.tcmp.tiapi.invoice.service.InvoiceSettlementService;
import com.tcmp.tiapi.invoice.util.EncodedAccountParser;
import com.tcmp.tiapi.program.model.ProgramExtension;
import com.tcmp.tiapi.shared.utils.MonetaryAmountUtils;
import com.tcmp.tiapi.ti.dto.request.AckServiceRequest;
import com.tcmp.tiapi.ti.route.TICCIncomingStrategy;
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
import com.tcmp.tiapi.titoapigee.operationalgateway.OperationalGatewayService;
import com.tcmp.tiapi.titoapigee.operationalgateway.exception.OperationalGatewayException;
import com.tcmp.tiapi.titoapigee.operationalgateway.model.InvoiceEmailEvent;
import com.tcmp.tiapi.titoapigee.operationalgateway.model.InvoiceEmailInfo;
import com.tcmp.tiapi.titoapigee.paymentexecution.PaymentExecutionService;
import com.tcmp.tiapi.titoapigee.paymentexecution.dto.response.BusinessAccountTransfersResponse;
import com.tcmp.tiapi.titoapigee.paymentexecution.dto.response.TransferResponseError;
import com.tcmp.tiapi.titoapigee.paymentexecution.exception.PaymentExecutionException;
import jakarta.annotation.Nullable;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InvoiceSettlementFlowStrategy implements TICCIncomingStrategy {
  private final InvoiceSettlementService invoiceSettlementService;
  private final CorporateLoanService corporateLoanService;
  private final PaymentExecutionService paymentExecutionService;
  private final OperationalGatewayService operationalGatewayService;
  private final BusinessBankingService businessBankingService;

  @Override
  public void handleServiceRequest(AckServiceRequest<?> serviceRequest) {
    CreateDueInvoiceEventMessage message = (CreateDueInvoiceEventMessage) serviceRequest.getBody();
    BigDecimal paymentAmountInCents = new BigDecimal(message.getPaymentAmount());
    BigDecimal paymentAmount = MonetaryAmountUtils.convertCentsToDollars(paymentAmountInCents);

    Customer buyer = invoiceSettlementService.findCustomerByMnemonic(message.getBuyerIdentifier());
    Customer seller =
        invoiceSettlementService.findCustomerByMnemonic(message.getSellerIdentifier());
    ProgramExtension programExtension =
        invoiceSettlementService.findProgrammeExtensionByIdOrDefault(message.getProgramme());
    InvoiceMaster invoice = invoiceSettlementService.findInvoiceByMasterRef(message.getMasterRef());
    ProductMasterExtension invoiceExtension =
        invoiceSettlementService.findProductMasterExtensionByMasterId(invoice.getId());
    EncodedAccountParser buyerAccountParser =
        new EncodedAccountParser(invoiceExtension.getFinanceAccount());

    boolean hasExtraFinancingDays = programExtension.getExtraFinancingDays() > 0;
    boolean hasBeenFinanced = invoiceSettlementService.invoiceHasLinkedFinanceEvent(invoice);

    // For Mvp, we ignore invoices with no extra financing days.
    if (!hasExtraFinancingDays) {
      log.info("Programe has no extra financing days, flow ended.");
      return;
    }

    try {
      // Second settlement case: end flow, we only notify credit to buyer.
      if (hasBeenFinanced) {
        notifyInvoiceStatusToCustomer(InvoiceEmailEvent.CREDITED, message, buyer, paymentAmount);
        return;
      }

      // Not financed invoices flow
      notifyInvoiceStatusToCustomer(InvoiceEmailEvent.SETTLED, message, seller, paymentAmount);
      createBuyerCreditOrThrowException(message, buyer, programExtension, buyerAccountParser);
      notifyInvoiceStatusToCustomer(InvoiceEmailEvent.CREDITED, message, buyer, paymentAmount);
      transferPaymentAmountFromBuyerToSellerOrThrowException(
          message, buyer, seller, buyerAccountParser);
      notifyInvoiceStatusToCustomer(InvoiceEmailEvent.PROCESSED, message, seller, paymentAmount);

      notifySettlementStatus(PayloadStatus.SUCCEEDED, message, invoice, null);
    } catch (CreditCreationException e) {
      log.error(e.getMessage());
      notifySettlementStatus(PayloadStatus.FAILED, message, invoice, e.getMessage());
    } catch (PaymentExecutionException e) {
      log.error(e.getTransferResponseError().title());
      notifySettlementStatus(
          PayloadStatus.FAILED, message, invoice, e.getTransferResponseError().title());
    }
  }

  private void notifyInvoiceStatusToCustomer(
      InvoiceEmailEvent event,
      CreateDueInvoiceEventMessage message,
      Customer customer,
      BigDecimal paymentAmount) {
    InvoiceEmailInfo creditedInvoiceInfo =
        invoiceSettlementService.buildInvoiceSettlementEmailInfo(
            event, message, customer, paymentAmount);
    try {
      operationalGatewayService.sendNotificationRequest(creditedInvoiceInfo);
    } catch (OperationalGatewayException e) {
      log.error(e.getMessage());
    }
  }

  private void createBuyerCreditOrThrowException(
      CreateDueInvoiceEventMessage message,
      Customer buyer,
      ProgramExtension programExtension,
      EncodedAccountParser buyerAccountParser)
      throws CreditCreationException {
    log.info("Started credit creation.");
    DistributorCreditResponse creditResponse =
        corporateLoanService.createCredit(
            invoiceSettlementService.buildDistributorCreditRequest(
                message, buyer, programExtension, buyerAccountParser));
    Error creditError = creditResponse.data().error();

    boolean hasBeenCredited = creditError != null && creditError.hasNoError();
    if (!hasBeenCredited) {
      String creditErrorMessage =
          creditError != null ? creditError.message() : "Credit creation failed.";
      throw new CreditCreationException(creditErrorMessage);
    }
  }

  private void transferPaymentAmountFromBuyerToSellerOrThrowException(
      CreateDueInvoiceEventMessage message,
      Customer buyer,
      Customer seller,
      EncodedAccountParser buyerAccountParser)
      throws PaymentExecutionException {
    log.info("Started buyer to seller transaction.");
    Account sellerAccount =
        invoiceSettlementService.findAccountByCustomerMnemonic(message.getSellerIdentifier());
    EncodedAccountParser sellerAccountParser =
        new EncodedAccountParser(sellerAccount.getExternalAccountNumber());

    BusinessAccountTransfersResponse buyerToBglTransactionResponse =
        paymentExecutionService.makeTransactionRequest(
            invoiceSettlementService.buildBuyerToBglTransactionRequest(
                message, seller, buyerAccountParser));

    BusinessAccountTransfersResponse bglToSellerTransactionResponse =
        paymentExecutionService.makeTransactionRequest(
            invoiceSettlementService.buildBglToSellerTransaction(
                message, buyer, sellerAccountParser));

    boolean isBuyerToSellerTransactionOk =
        buyerToBglTransactionResponse != null
            && bglToSellerTransactionResponse != null
            && buyerToBglTransactionResponse.isOk()
            && bglToSellerTransactionResponse.isOk();
    if (!isBuyerToSellerTransactionOk) {
      String defaultTransferError =
          "Could not transfer invoice payment amount from buyer to seller.";
      throw new PaymentExecutionException(
          TransferResponseError.builder().title(defaultTransferError).build());
    }
  }

  private void notifySettlementStatus(
      PayloadStatus status,
      CreateDueInvoiceEventMessage message,
      InvoiceMaster invoice,
      @Nullable String error) {
    List<String> errors = error == null ? null : List.of(error);

    OperationalGatewayRequestPayload payload =
        OperationalGatewayRequestPayload.builder()
            .status(status.getValue())
            .invoice(
                PayloadInvoice.builder()
                    .batchId(invoice.getBatchId().trim())
                    .reference(message.getInvoiceNumber())
                    .sellerMnemonic(message.getSellerIdentifier())
                    .build())
            .details(new PayloadDetails(errors, null, null))
            .build();

    businessBankingService.notifyEvent(OperationalGatewayProcessCode.INVOICE_SETTLEMENT, payload);
  }
}
