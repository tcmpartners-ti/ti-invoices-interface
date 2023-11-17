package com.tcmp.tiapi.invoice.route;

import com.tcmp.tiapi.customer.model.Account;
import com.tcmp.tiapi.customer.model.Customer;
import com.tcmp.tiapi.invoice.dto.ti.financeack.FinanceAckMessage;
import com.tcmp.tiapi.invoice.model.ProductMasterExtension;
import com.tcmp.tiapi.invoice.service.InvoiceFinancingService;
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
public class InvoiceFinanceResultFlowRouteBuilder extends RouteBuilder {
  private final InvoiceFinancingService invoiceFinancingService;
  private final CorporateLoanService corporateLoanService;
  private final PaymentExecutionService paymentExecutionService;
  private final OperationalGatewayService operationalGatewayService;

  private final String uriFrom;

  @Override
  public void configure() {
    from(uriFrom).routeId("invoiceFinanceResultFlow")
      .log("Started invoice finance flow.")
      .process().body(AckServiceRequest.class, this::startInvoiceFinancingFlow)
      .log("Completed invoice finance flow.")
      .end();
  }

  private void startInvoiceFinancingFlow(AckServiceRequest<FinanceAckMessage> serviceRequest) {
    if (serviceRequest == null) throw new UnrecoverableApiGeeRequestException("Message with no body received.");
    FinanceAckMessage financeMessage = serviceRequest.getBody();
    String invoiceReference = financeMessage.getInvoiceArray().get(0).getInvoiceReference();
    BigDecimal financeDealAmountInCents = new BigDecimal(financeMessage.getFinanceDealAmount());
    BigDecimal financeDealAmount = MonetaryAmountUtils.convertCentsToDollars(financeDealAmountInCents);

    Customer buyer = invoiceFinancingService.findCustomerByMnemonic(financeMessage.getBuyerIdentifier());
    Customer seller = invoiceFinancingService.findCustomerByMnemonic(financeMessage.getSellerIdentifier());
    ProductMasterExtension invoiceExtension = invoiceFinancingService.findProductMasterExtensionByMasterReference(invoiceReference);
    Account sellerAccount = invoiceFinancingService.findAccountByCustomerMnemonic(financeMessage.getSellerIdentifier());
    ProgramExtension programExtension = invoiceFinancingService.findByProgrammeIdOrDefault(financeMessage.getProgramme());

    EncodedAccountParser buyerAccountParser = new EncodedAccountParser(invoiceExtension.getFinanceAccount());
    EncodedAccountParser sellerAccountParser = new EncodedAccountParser(sellerAccount.getExternalAccountNumber());

    InvoiceEmailInfo financedInvoiceInfo = invoiceFinancingService.buildInvoiceFinancingEmailInfo(
      financeMessage, seller, InvoiceEmailEvent.FINANCED, financeDealAmount);
    operationalGatewayService.sendNotificationRequest(financedInvoiceInfo);

    log.info("Starting credit creation.");
    DistributorCreditResponse creditResponse = corporateLoanService.createCredit(
      invoiceFinancingService.buildDistributorCreditRequest(financeMessage, programExtension, buyer, buyerAccountParser));
    Error creditResponseError = creditResponse.data().error();

    boolean hasBeenCredited = creditResponseError != null && creditResponseError.hasNoError();
    if (!hasBeenCredited) {
      log.error("Could not create credit.");
      return;
    }

    log.info("Starting buyer to seller transaction.");
    boolean buyerToSellerTransactionSuccessful = transferCreditAmountFromBuyerToSeller(
      creditResponse, financeMessage, buyerAccountParser, sellerAccountParser);
    if (!buyerToSellerTransactionSuccessful) {
      log.error("Could not transfer from buyer to seller.");
      return;
    }

    InvoiceEmailInfo processedInvoiceInfo = invoiceFinancingService.buildInvoiceFinancingEmailInfo(
      financeMessage, seller, InvoiceEmailEvent.PROCESSED, financeDealAmount);
    operationalGatewayService.sendNotificationRequest(processedInvoiceInfo);
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

      log.info(
        "[Anchor -> Bgl]: {} [Bgl -> Seller]: {} DisbursementAmount={}",
        buyerToBglStatus,
        bglToSellerStatus,
        distributorCreditResponse.data().disbursementAmount()
      );

      return "OK".equals(buyerToBglStatus) && "OK".equals(bglToSellerStatus);
    } catch (PaymentExecutionException e) {
      return false;
    }
  }
}
