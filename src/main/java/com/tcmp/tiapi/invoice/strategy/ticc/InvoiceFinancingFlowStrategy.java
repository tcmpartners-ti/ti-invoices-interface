package com.tcmp.tiapi.invoice.strategy.ticc;

import com.tcmp.tiapi.customer.model.Account;
import com.tcmp.tiapi.customer.model.Customer;
import com.tcmp.tiapi.customer.repository.AccountRepository;
import com.tcmp.tiapi.customer.repository.CustomerRepository;
import com.tcmp.tiapi.invoice.dto.ti.financeack.FinanceAckMessage;
import com.tcmp.tiapi.invoice.model.EventExtension;
import com.tcmp.tiapi.invoice.model.InvoiceMaster;
import com.tcmp.tiapi.invoice.model.ProductMasterExtension;
import com.tcmp.tiapi.invoice.repository.EventExtensionRepository;
import com.tcmp.tiapi.invoice.repository.InvoiceRepository;
import com.tcmp.tiapi.invoice.repository.ProductMasterExtensionRepository;
import com.tcmp.tiapi.invoice.util.EncodedAccountParser;
import com.tcmp.tiapi.program.model.ProgramExtension;
import com.tcmp.tiapi.program.repository.ProgramExtensionRepository;
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
import com.tcmp.tiapi.titoapigee.corporateloan.dto.request.*;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.response.DistributorCreditResponse;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.response.Error;
import com.tcmp.tiapi.titoapigee.corporateloan.exception.CreditCreationException;
import com.tcmp.tiapi.titoapigee.operationalgateway.OperationalGatewayService;
import com.tcmp.tiapi.titoapigee.operationalgateway.exception.OperationalGatewayException;
import com.tcmp.tiapi.titoapigee.operationalgateway.model.InvoiceEmailEvent;
import com.tcmp.tiapi.titoapigee.operationalgateway.model.InvoiceEmailInfo;
import com.tcmp.tiapi.titoapigee.paymentexecution.PaymentExecutionService;
import com.tcmp.tiapi.titoapigee.paymentexecution.dto.request.TransactionRequest;
import com.tcmp.tiapi.titoapigee.paymentexecution.dto.request.TransactionType;
import com.tcmp.tiapi.titoapigee.paymentexecution.dto.response.BusinessAccountTransfersResponse;
import com.tcmp.tiapi.titoapigee.paymentexecution.exception.PaymentExecutionException;
import jakarta.annotation.Nullable;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InvoiceFinancingFlowStrategy implements TICCIncomingStrategy {
  private final EventExtensionRepository eventExtensionRepository;
  private final ProductMasterExtensionRepository productMasterExtensionRepository;
  private final ProgramExtensionRepository programExtensionRepository;
  private final CustomerRepository customerRepository;
  private final AccountRepository accountRepository;
  private final InvoiceRepository invoiceRepository;

  private final CorporateLoanService corporateLoanService;
  private final PaymentExecutionService paymentExecutionService;
  private final OperationalGatewayService operationalGatewayService;
  private final BusinessBankingService businessBankingService;

  @Value("${bp.service.payment-execution.bgl-account}")
  private String bglAccount;

  @Override
  public void handleServiceRequest(AckServiceRequest<?> serviceRequest) {
    FinanceAckMessage financeMessage = (FinanceAckMessage) serviceRequest.getBody();
    String masterReference = financeMessage.getInvoiceArray().get(0).getInvoiceReference();
    BigDecimal financeDealAmountInCents = new BigDecimal(financeMessage.getFinanceDealAmount());
    BigDecimal financeDealAmount =
        MonetaryAmountUtils.convertCentsToDollars(financeDealAmountInCents);

    Customer buyer = findCustomerByMnemonic(financeMessage.getBuyerIdentifier());
    Customer seller = findCustomerByMnemonic(financeMessage.getSellerIdentifier());
    ProductMasterExtension invoiceExtension = findMasterExtensionByReference(masterReference);
    InvoiceMaster invoice = findInvoiceByMasterReference(masterReference);
    ProgramExtension programExtension = findByProgrammeIdOrDefault(financeMessage.getProgramme());

    try {
      EncodedAccountParser buyerAccountParser =
          new EncodedAccountParser(invoiceExtension.getFinanceAccount());
      EncodedAccountParser sellerAccountParser = findSelectedSellerAccountOrDefault(financeMessage);

      notifyInvoiceStatusToSeller(
          InvoiceEmailEvent.FINANCED, financeMessage, seller, financeDealAmount);
      createAndTransferBuyerCreditAmountFromBuyerToSeller(
          financeMessage, programExtension, buyer, buyerAccountParser, sellerAccountParser);
      simulateAndTransferSellerCreditAmountFromSellerToBuyer(
          financeMessage, programExtension, buyer, buyerAccountParser, sellerAccountParser);
      notifyInvoiceStatusToSeller(
          InvoiceEmailEvent.PROCESSED, financeMessage, seller, financeDealAmount);

      notifyFinanceStatus(PayloadStatus.SUCCEEDED, financeMessage, invoice, null);
    } catch (CreditCreationException
        | PaymentExecutionException
        | EncodedAccountParser.AccountDecodingException e) {
      log.error(e.getMessage());
      notifyFinanceStatus(PayloadStatus.FAILED, financeMessage, invoice, e.getMessage());
    }
  }

  private void notifyInvoiceStatusToSeller(
      InvoiceEmailEvent event,
      FinanceAckMessage financeMessage,
      Customer seller,
      BigDecimal financeDealAmount) {
    try {
      InvoiceEmailInfo financedInvoiceInfo =
          buildInvoiceFinancingEmailInfo(event, financeMessage, seller, financeDealAmount);
      operationalGatewayService.sendNotificationRequest(financedInvoiceInfo);
    } catch (OperationalGatewayException e) {
      log.error(e.getMessage());
    }
  }

  private InvoiceEmailInfo buildInvoiceFinancingEmailInfo(
      InvoiceEmailEvent event,
      FinanceAckMessage invoiceFinanceAck,
      Customer customer,
      BigDecimal amount) {
    return InvoiceEmailInfo.builder()
        .customerMnemonic(invoiceFinanceAck.getSellerIdentifier())
        .customerEmail(customer.getAddress().getCustomerEmail().trim())
        .customerName(customer.getFullName().trim())
        .date(invoiceFinanceAck.getStartDate())
        .action(event.getValue())
        .invoiceCurrency(invoiceFinanceAck.getFinanceDealCurrency())
        .invoiceNumber(invoiceFinanceAck.getTheirRef())
        .amount(amount)
        .build();
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

  private Customer findCustomerByMnemonic(String customerMnemonic) {
    return customerRepository
        .findFirstByIdMnemonic(customerMnemonic)
        .orElseThrow(
            () ->
                new EntityNotFoundException(
                    "Could not find customer with mnemonic " + customerMnemonic));
  }

  private ProductMasterExtension findMasterExtensionByReference(String invoiceMasterReference) {
    return productMasterExtensionRepository
        .findByMasterReference(invoiceMasterReference)
        .orElseThrow(
            () ->
                new EntityNotFoundException(
                    "Could not find account for the given invoice master."));
  }

  private InvoiceMaster findInvoiceByMasterReference(String invoiceMasterReference) {
    return invoiceRepository
        .findByProductMasterMasterReference(invoiceMasterReference)
        .orElseThrow(
            () ->
                new EntityNotFoundException(
                    "Could not find invoice for the given invoice master."));
  }

  private ProgramExtension findByProgrammeIdOrDefault(String programmeId) {
    return programExtensionRepository
        .findByProgrammeId(programmeId)
        .orElse(
            ProgramExtension.builder()
                .programmeId(programmeId)
                .extraFinancingDays(0)
                .requiresExtraFinancing(false)
                .build());
  }

  private EncodedAccountParser findSelectedSellerAccountOrDefault(
      FinanceAckMessage financeMessage) {
    return eventExtensionRepository
        .findByMasterReference(financeMessage.getMasterRef())
        .map(EventExtension::getFinanceSellerAccount)
        .filter(account -> !account.isBlank())
        .map(EncodedAccountParser::new)
        .orElseGet(
            () -> {
              String sellerMnemonic = financeMessage.getSellerIdentifier();
              Account defaultSellerAccount = findAccountByCustomerMnemonic(sellerMnemonic);
              return new EncodedAccountParser(defaultSellerAccount.getExternalAccountNumber());
            });
  }

  private Account findAccountByCustomerMnemonic(String customerMnemonic) {
    return accountRepository
        .findByTypeAndCustomerMnemonic("CA", customerMnemonic)
        .orElseThrow(
            () ->
                new EntityNotFoundException(
                    "Could not find account for customer " + customerMnemonic));
  }

  private void createAndTransferBuyerCreditAmountFromBuyerToSeller(
      FinanceAckMessage financeMessage,
      ProgramExtension programExtension,
      Customer buyer,
      EncodedAccountParser buyerAccountParser,
      EncodedAccountParser sellerAccountParser)
      throws CreditCreationException, PaymentExecutionException {
    log.info("Starting credit creation.");
    DistributorCreditResponse distributorCreditResponse =
        corporateLoanService.createCredit(
            buildDistributorCreditRequest(
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
      throw new PaymentExecutionException(
          "Could not transfer invoice payment amount from buyer to seller.");
    }
  }

  private boolean transferCreditAmountFromBuyerToSeller(
      DistributorCreditResponse creditResponse,
      FinanceAckMessage financeMessage,
      EncodedAccountParser buyerAccountParser,
      EncodedAccountParser sellerAccountParser) {
    TransactionRequest buyerToBglRequest =
        buildBuyerToBglTransactionRequest(creditResponse, financeMessage, buyerAccountParser);
    BusinessAccountTransfersResponse buyerToBglResponse =
        paymentExecutionService.makeTransactionRequest(buyerToBglRequest);

    TransactionRequest bglToSellerRequest =
        buildBglToSellerTransactionRequest(creditResponse, financeMessage, sellerAccountParser);
    BusinessAccountTransfersResponse bglToSellerResponse =
        paymentExecutionService.makeTransactionRequest(bglToSellerRequest);

    if (buyerToBglResponse == null || bglToSellerResponse == null) return false;
    return buyerToBglResponse.isOk() && bglToSellerResponse.isOk();
  }

  private TransactionRequest buildBuyerToBglTransactionRequest(
      DistributorCreditResponse distributorCreditResponse,
      FinanceAckMessage financeMessage,
      EncodedAccountParser buyerAccount) {
    String concept =
        String.format(
            "Descuento Factura %s %s",
            financeMessage.getTheirRef(), financeMessage.getSellerName());
    String currency = financeMessage.getPaymentDetails().getCurrency();
    BigDecimal amount = BigDecimal.valueOf(distributorCreditResponse.data().disbursementAmount());

    return TransactionRequest.from(
        TransactionType.CLIENT_TO_BGL,
        buyerAccount.getAccount(),
        bglAccount,
        concept,
        currency,
        amount);
  }

  private TransactionRequest buildBglToSellerTransactionRequest(
      DistributorCreditResponse distributorCreditResponse,
      FinanceAckMessage financeMessage,
      EncodedAccountParser sellerAccount) {
    String concept =
        String.format(
            "Pago Factura %s %s", financeMessage.getTheirRef(), financeMessage.getBuyerName());
    String currency = financeMessage.getPaymentDetails().getCurrency();
    BigDecimal amount = BigDecimal.valueOf(distributorCreditResponse.data().disbursementAmount());

    return TransactionRequest.from(
        TransactionType.BGL_TO_CLIENT,
        bglAccount,
        sellerAccount.getAccount(),
        concept,
        currency,
        amount);
  }

  private void simulateAndTransferSellerCreditAmountFromSellerToBuyer(
      FinanceAckMessage financeMessage,
      ProgramExtension programExtension,
      Customer buyer,
      EncodedAccountParser buyerAccountParser,
      EncodedAccountParser sellerAccountParser)
      throws CreditCreationException, PaymentExecutionException {
    log.info("Starting credit simulation.");
    DistributorCreditResponse sellerCreditSimulationResponse =
        corporateLoanService.simulateCredit(
            buildDistributorCreditRequest(
                financeMessage, programExtension, buyer, buyerAccountParser, true));
    Error creditError = sellerCreditSimulationResponse.data().error();

    boolean hasBeenCredited = creditError != null && creditError.hasNoError();
    if (!hasBeenCredited) {
      String creditErrorMessage =
          creditError != null ? creditError.message() : "Credit simulation failed.";
      throw new CreditCreationException(creditErrorMessage);
    }

    log.info("Starting seller to buyer taxes and solca transaction.");
    boolean isSellerToBuyerTransactionOk =
        transferSolcaAndTaxesAmountFromSellerToBuyer(
            sellerCreditSimulationResponse,
            financeMessage,
            sellerAccountParser,
            buyerAccountParser);
    if (!isSellerToBuyerTransactionOk) {
      throw new PaymentExecutionException(
          "Could not transfer solca plus taxes amount from seller to buyer.");
    }
  }

  private DistributorCreditRequest buildDistributorCreditRequest(
      FinanceAckMessage invoiceFinanceAck,
      ProgramExtension programExtension,
      Customer buyer,
      EncodedAccountParser buyerAccountParser,
      boolean isSellerCentered) {
    return DistributorCreditRequest.builder()
        .commercialTrade(new CommercialTrade(buyer.getType().trim()))
        .customer(
            com.tcmp.tiapi.titoapigee.corporateloan.dto.request.Customer.builder()
                .customerId(buyer.getNumber().trim())
                .documentNumber(buyer.getId().getMnemonic().trim())
                .fullName(buyer.getFullName().trim())
                .documentType(buyer.getBankCode1().trim())
                .build())
        .disbursement(
            Disbursement.builder()
                .accountNumber(buyerAccountParser.getAccount())
                .accountType(buyerAccountParser.getType())
                .bankId("010")
                .form("N/C")
                .build())
        .amount(getFinanceDealAmountFromMessage(invoiceFinanceAck))
        .effectiveDate(invoiceFinanceAck.getStartDate())
        .term(
            isSellerCentered
                ? calculateCreditTermForSeller(invoiceFinanceAck)
                : calculateCreditTermForBuyer(invoiceFinanceAck, programExtension))
        .termPeriodType(new TermPeriodType("D"))
        .amortizationPaymentPeriodType(new AmortizationPaymentPeriodType("FIN"))
        .interestPayment(new InterestPayment("FIN", new GracePeriod("V", "001")))
        .maturityForm("C99")
        .quotaMaturityCriteria("*NO")
        .references(List.of())
        .tax(
            Tax.builder()
                .code("L")
                .paymentForm(new PaymentForm("C"))
                .rate(BigDecimal.ZERO)
                .amount(BigDecimal.ZERO)
                .build())
        .build();
  }

  private int calculateCreditTermForBuyer(
      FinanceAckMessage invoiceFinanceMessage, ProgramExtension programExtension) {
    int extraFinancingDays = programExtension.getExtraFinancingDays();
    return extraFinancingDays + calculateCreditTermForSeller(invoiceFinanceMessage);
  }

  private int calculateCreditTermForSeller(FinanceAckMessage invoiceFinanceMessage) {
    LocalDate startDate = LocalDate.parse(invoiceFinanceMessage.getStartDate());
    LocalDate maturityDate = LocalDate.parse(invoiceFinanceMessage.getMaturityDate());

    return (int) ChronoUnit.DAYS.between(startDate, maturityDate);
  }

  private boolean transferSolcaAndTaxesAmountFromSellerToBuyer(
      DistributorCreditResponse creditResponse,
      FinanceAckMessage financeMessage,
      EncodedAccountParser sellerAccount,
      EncodedAccountParser buyerAccount) {

    TransactionRequest sellerToBglRequest =
        buildSellerToBglTaxesTransactionRequest(creditResponse, financeMessage, sellerAccount);
    BusinessAccountTransfersResponse sellerToBglResponse =
        paymentExecutionService.makeTransactionRequest(sellerToBglRequest);

    TransactionRequest bglToBuyerRequest =
        buildBglToBuyerTaxesTransactionRequest(creditResponse, financeMessage, buyerAccount);
    BusinessAccountTransfersResponse bglToBuyerResponse =
        paymentExecutionService.makeTransactionRequest(bglToBuyerRequest);

    if (sellerToBglResponse == null || bglToBuyerResponse == null) return false;
    return sellerToBglResponse.isOk() && bglToBuyerResponse.isOk();
  }

  private TransactionRequest buildSellerToBglTaxesTransactionRequest(
      DistributorCreditResponse sellerCreditResponse,
      FinanceAckMessage financeMessage,
      EncodedAccountParser sellerAccount) {

    String concept =
        String.format(
            "Intereses y Solca Factura %s %s",
            financeMessage.getTheirRef(), financeMessage.getBuyerName());
    String currency = financeMessage.getPaymentDetails().getCurrency();
    BigDecimal amount =
        calculateSolcaAndTaxesFromCreditResponse(sellerCreditResponse, financeMessage);

    return TransactionRequest.from(
        TransactionType.CLIENT_TO_BGL,
        sellerAccount.getAccount(),
        bglAccount,
        concept,
        currency,
        amount);
  }

  private TransactionRequest buildBglToBuyerTaxesTransactionRequest(
      DistributorCreditResponse sellerCreditResponse,
      FinanceAckMessage financeMessage,
      EncodedAccountParser buyerAccount) {

    String concept =
        String.format(
            "Intereses y Solca Factura %s %s",
            financeMessage.getTheirRef(), financeMessage.getSellerName());
    String currency = financeMessage.getPaymentDetails().getCurrency();
    BigDecimal amount =
        calculateSolcaAndTaxesFromCreditResponse(sellerCreditResponse, financeMessage);

    return TransactionRequest.from(
        TransactionType.BGL_TO_CLIENT,
        bglAccount,
        buyerAccount.getAccount(),
        concept,
        currency,
        amount);
  }

  private BigDecimal calculateSolcaAndTaxesFromCreditResponse(
      DistributorCreditResponse sellerCreditResponse, FinanceAckMessage financeMessage) {
    BigDecimal financeDealAmount = getFinanceDealAmountFromMessage(financeMessage);
    // totalInstallmentsAmount = Invoice value + taxes + solca
    BigDecimal totalInstallmentsAmount =
        BigDecimal.valueOf(sellerCreditResponse.data().totalInstallmentsAmount());

    return totalInstallmentsAmount.subtract(financeDealAmount);
  }

  private BigDecimal getFinanceDealAmountFromMessage(FinanceAckMessage invoiceFinanceAck) {
    BigDecimal financeDealAmountInCents = new BigDecimal(invoiceFinanceAck.getFinanceDealAmount());
    return MonetaryAmountUtils.convertCentsToDollars(financeDealAmountInCents);
  }
}
