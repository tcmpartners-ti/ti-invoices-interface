package com.tcmp.tiapi.invoice.route;

import com.tcmp.tiapi.customer.model.Account;
import com.tcmp.tiapi.customer.model.Customer;
import com.tcmp.tiapi.invoice.dto.ti.financeack.FinanceAckMessage;
import com.tcmp.tiapi.invoice.model.InvoiceMaster;
import com.tcmp.tiapi.invoice.model.ProductMasterExtension;
import com.tcmp.tiapi.invoice.service.InvoiceFinancingService;
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
import com.tcmp.tiapi.titoapigee.paymentexecution.dto.response.TransferResponseError;
import com.tcmp.tiapi.titoapigee.paymentexecution.exception.PaymentExecutionException;
import jakarta.annotation.Nullable;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;

@RequiredArgsConstructor
public class InvoiceFinanceResultFlowRouteBuilder extends RouteBuilder {
  private final InvoiceFinancingService invoiceFinancingService;
  private final CorporateLoanService corporateLoanService;
  private final PaymentExecutionService paymentExecutionService;
  private final OperationalGatewayService operationalGatewayService;
  private final BusinessBankingService businessBankingService;

  private final String uriFrom;

  @Override
  public void configure() {
    from(uriFrom)
        .routeId("invoiceFinanceResultFlow")
        .log("Started invoice finance flow.")
        .process()
        .body(AckServiceRequest.class, this::startInvoiceFinancingFlow)
        .log("Completed invoice finance flow.")
        .end();
  }

  private void startInvoiceFinancingFlow(AckServiceRequest<FinanceAckMessage> serviceRequest) {
    if (serviceRequest == null)
      throw new UnrecoverableApiGeeRequestException("Message with no body received.");
    FinanceAckMessage financeMessage = serviceRequest.getBody();
    String masterReference = financeMessage.getInvoiceArray().get(0).getInvoiceReference();
    BigDecimal financeDealAmountInCents = new BigDecimal(financeMessage.getFinanceDealAmount());
    BigDecimal financeDealAmount =
        MonetaryAmountUtils.convertCentsToDollars(financeDealAmountInCents);

    Customer buyer =
        invoiceFinancingService.findCustomerByMnemonic(financeMessage.getBuyerIdentifier());
    Customer seller =
        invoiceFinancingService.findCustomerByMnemonic(financeMessage.getSellerIdentifier());
    ProductMasterExtension invoiceExtension =
        invoiceFinancingService.findProductMasterExtensionByMasterReference(masterReference);
    InvoiceMaster invoice = invoiceFinancingService.findInvoiceByMasterReference(masterReference);
    Account sellerAccount =
        invoiceFinancingService.findAccountByCustomerMnemonic(financeMessage.getSellerIdentifier());
    ProgramExtension programExtension =
        invoiceFinancingService.findByProgrammeIdOrDefault(financeMessage.getProgramme());

    EncodedAccountParser buyerAccountParser =
        new EncodedAccountParser(invoiceExtension.getFinanceAccount());
    EncodedAccountParser sellerAccountParser =
        new EncodedAccountParser(sellerAccount.getExternalAccountNumber());

    try {
      notifyInvoiceStatusToSeller(
          InvoiceEmailEvent.FINANCED, financeMessage, seller, financeDealAmount);
      createAndTransferCreditAmountFromBuyerToSellerOrThrowException(
          financeMessage, programExtension, buyer, buyerAccountParser, sellerAccountParser);
      simulateCreditWithSolcaPlusTaxesAndTransferAmountFromSellerToBuyerOrThrowException(
          financeMessage, programExtension, buyer, buyerAccountParser, sellerAccountParser);
      notifyInvoiceStatusToSeller(
          InvoiceEmailEvent.PROCESSED, financeMessage, seller, financeDealAmount);

      notifyFinanceStatus(PayloadStatus.SUCCEEDED, financeMessage, invoice, null);
    } catch (CreditCreationException e) {
      log.error(e.getMessage());
      notifyFinanceStatus(PayloadStatus.FAILED, financeMessage, invoice, e.getMessage());
    } catch (PaymentExecutionException e) {
      log.error(e.getTransferResponseError().title());
      notifyFinanceStatus(
          PayloadStatus.FAILED, financeMessage, invoice, e.getTransferResponseError().title());
    }
  }

  private void notifyInvoiceStatusToSeller(
      InvoiceEmailEvent event,
      FinanceAckMessage financeMessage,
      Customer seller,
      BigDecimal financeDealAmount) {
    InvoiceEmailInfo financedInvoiceInfo =
        invoiceFinancingService.buildInvoiceFinancingEmailInfo(
            event, financeMessage, seller, financeDealAmount);
    operationalGatewayService.sendNotificationRequest(financedInvoiceInfo);
  }

  private void createAndTransferCreditAmountFromBuyerToSellerOrThrowException(
      FinanceAckMessage financeMessage,
      ProgramExtension programExtension,
      Customer buyer,
      EncodedAccountParser buyerAccountParser,
      EncodedAccountParser sellerAccountParser)
      throws CreditCreationException, PaymentExecutionException {
    log.info("Starting credit creation.");
    DistributorCreditResponse distributorCreditResponse =
        corporateLoanService.createCredit(
            invoiceFinancingService.buildDistributorCreditRequest(
                financeMessage, programExtension, buyer, buyerAccountParser, false));
    Error creditError = distributorCreditResponse.data().error();

    boolean hasBeenCredited = creditError != null && creditError.hasNoError();
    if (!hasBeenCredited) {
      String creditErrorMessage =
          creditError != null ? creditError.message() : "Credit creation failed.";
      throw new CreditCreationException(creditErrorMessage);
    }

    log.info("Starting buyer to seller transaction.");
    boolean buyerToSellerTransactionSuccessful =
        transferCreditAmountFromBuyerToSeller(
            distributorCreditResponse, financeMessage, buyerAccountParser, sellerAccountParser);
    if (!buyerToSellerTransactionSuccessful) {
      String transferError = "Could not transfer invoice payment amount from buyer to seller.";
      throw new PaymentExecutionException(
          TransferResponseError.builder().title(transferError).build());
    }
  }

  private boolean transferCreditAmountFromBuyerToSeller(
      DistributorCreditResponse distributorCreditResponse,
      FinanceAckMessage invoiceFinanceMessage,
      EncodedAccountParser buyerAccount,
      EncodedAccountParser sellerAccount) {
    BusinessAccountTransfersResponse buyerToBglResponse =
        paymentExecutionService.makeTransactionRequest(
            invoiceFinancingService.buildBuyerToBglTransactionRequest(
                distributorCreditResponse, invoiceFinanceMessage, buyerAccount));

    BusinessAccountTransfersResponse bglToSellerResponse =
        paymentExecutionService.makeTransactionRequest(
            invoiceFinancingService.buildBglToSellerTransactionRequest(
                distributorCreditResponse, invoiceFinanceMessage, sellerAccount));

    if (buyerToBglResponse == null || bglToSellerResponse == null) return false;
    return buyerToBglResponse.isOk() && bglToSellerResponse.isOk();
  }

  private void simulateCreditWithSolcaPlusTaxesAndTransferAmountFromSellerToBuyerOrThrowException(
      FinanceAckMessage financeMessage,
      ProgramExtension programExtension,
      Customer buyer,
      EncodedAccountParser buyerAccountParser,
      EncodedAccountParser sellerAccountParser)
      throws CreditCreationException, PaymentExecutionException {
    log.info("Starting credit simulation.");
    DistributorCreditResponse sellerCreditSimulationResponse =
        corporateLoanService.createCredit(
            invoiceFinancingService.buildDistributorCreditRequest(
                financeMessage, programExtension, buyer, buyerAccountParser, true));
    Error creditError = sellerCreditSimulationResponse.data().error();

    boolean hasBeenCredited = creditError != null && creditError.hasNoError();
    if (!hasBeenCredited) {
      String creditErrorMessage =
          creditError != null ? creditError.message() : "Credit simulation failed.";
      throw new CreditCreationException(creditErrorMessage);
    }

    log.info("Starting buyer to seller taxes and solca transaction.");
    boolean buyerToSellerTransactionSuccessful =
        transferSolcaAndTaxesAmountFromSellerToBuyer(
            sellerCreditSimulationResponse,
            financeMessage,
            sellerAccountParser,
            buyerAccountParser);
    if (!buyerToSellerTransactionSuccessful) {
      String transferError = "Could not transfer solca plus taxes amount from seller to buyer.";
      throw new PaymentExecutionException(
          TransferResponseError.builder().title(transferError).build());
    }
  }

  private boolean transferSolcaAndTaxesAmountFromSellerToBuyer(
      DistributorCreditResponse sellerCreditResponse,
      FinanceAckMessage financeMessage,
      EncodedAccountParser sellerAccount,
      EncodedAccountParser buyerAccount) {

    BusinessAccountTransfersResponse sellerToBglResponse =
        paymentExecutionService.makeTransactionRequest(
            invoiceFinancingService.buildSellerToBglSolcaAndTaxesTransactionRequest(
                sellerCreditResponse, financeMessage, sellerAccount));

    BusinessAccountTransfersResponse bglToBuyerResponse =
        paymentExecutionService.makeTransactionRequest(
            invoiceFinancingService.buildBglToBuyerSolcaAndTaxesTransactionRequest(
                sellerCreditResponse, financeMessage, buyerAccount));

    if (sellerToBglResponse == null || bglToBuyerResponse == null) return false;
    return sellerToBglResponse.isOk() && bglToBuyerResponse.isOk();
  }

  private void notifyFinanceStatus(
      PayloadStatus status,
      FinanceAckMessage financeResultMessage,
      InvoiceMaster invoice,
      @Nullable String error) {
    List<String> errors = error == null ? null : List.of(error);

    OperationalGatewayRequestPayload payload =
        OperationalGatewayRequestPayload.builder()
            .status(status.getValue())
            .invoice(
                PayloadInvoice.builder()
                    .batchId(invoice.getBatchId().trim())
                    .reference(financeResultMessage.getTheirRef())
                    .sellerMnemonic(financeResultMessage.getSellerIdentifier())
                    .build())
            .details(new PayloadDetails(errors, null, null))
            .build();

    businessBankingService.notifyEvent(OperationalGatewayProcessCode.INVOICE_FINANCING, payload);
  }
}
