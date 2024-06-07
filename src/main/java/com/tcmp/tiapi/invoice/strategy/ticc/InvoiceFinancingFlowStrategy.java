package com.tcmp.tiapi.invoice.strategy.ticc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcmp.tiapi.customer.model.Account;
import com.tcmp.tiapi.customer.model.Customer;
import com.tcmp.tiapi.customer.repository.AccountRepository;
import com.tcmp.tiapi.customer.repository.CustomerRepository;
import com.tcmp.tiapi.invoice.dto.ti.financeack.FinanceAckMessage;
import com.tcmp.tiapi.invoice.exception.InconsistentInvoiceInformationException;
import com.tcmp.tiapi.invoice.model.EventExtension;
import com.tcmp.tiapi.invoice.model.InvoiceMaster;
import com.tcmp.tiapi.invoice.model.ProductMasterExtension;
import com.tcmp.tiapi.invoice.repository.EventExtensionRepository;
import com.tcmp.tiapi.invoice.repository.InvoiceRepository;
import com.tcmp.tiapi.invoice.repository.ProductMasterExtensionRepository;
import com.tcmp.tiapi.invoice.util.EncodedAccountParser;
import com.tcmp.tiapi.program.model.ProgramExtension;
import com.tcmp.tiapi.program.repository.ProgramExtensionRepository;
import com.tcmp.tiapi.shared.UUIDGenerator;
import com.tcmp.tiapi.shared.utils.MonetaryAmountUtils;
import com.tcmp.tiapi.ti.dto.request.AckServiceRequest;
import com.tcmp.tiapi.ti.route.ticc.TICCIncomingStrategy;
import com.tcmp.tiapi.titoapigee.businessbanking.BusinessBankingService;
import com.tcmp.tiapi.titoapigee.businessbanking.dto.request.OperationalGatewayRequestPayload;
import com.tcmp.tiapi.titoapigee.businessbanking.dto.request.PayloadDetails;
import com.tcmp.tiapi.titoapigee.businessbanking.dto.request.PayloadInvoice;
import com.tcmp.tiapi.titoapigee.businessbanking.dto.request.PayloadStatus;
import com.tcmp.tiapi.titoapigee.businessbanking.model.OperationalGatewayProcessCode;
import com.tcmp.tiapi.titoapigee.corporateloan.CorporateLoanService;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.CorporateLoanMapper;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.request.*;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.response.Amortization;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.response.Data;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.response.DistributorCreditResponse;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.response.Error;
import com.tcmp.tiapi.titoapigee.corporateloan.exception.CreditCreationException;
import com.tcmp.tiapi.titoapigee.operationalgateway.OperationalGatewayService;
import com.tcmp.tiapi.titoapigee.operationalgateway.model.InvoiceEmailEvent;
import com.tcmp.tiapi.titoapigee.operationalgateway.model.InvoiceEmailInfo;
import com.tcmp.tiapi.titofcm.dto.SinglePaymentMapper;
import com.tcmp.tiapi.titofcm.dto.request.SinglePaymentRequest;
import com.tcmp.tiapi.titofcm.dto.response.PaymentResultResponse;
import com.tcmp.tiapi.titofcm.dto.response.SinglePaymentResponse;
import com.tcmp.tiapi.titofcm.exception.SinglePaymentException;
import com.tcmp.tiapi.titofcm.model.InvoicePaymentCorrelationInfo;
import com.tcmp.tiapi.titofcm.repository.InvoicePaymentCorrelationInfoRepository;
import com.tcmp.tiapi.titofcm.service.SingleElectronicPaymentService;
import jakarta.annotation.Nullable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InvoiceFinancingFlowStrategy implements TICCIncomingStrategy {
  private final UUIDGenerator uuidGenerator;
  private final ObjectMapper objectMapper;

  private final AccountRepository accountRepository;
  private final CustomerRepository customerRepository;
  private final EventExtensionRepository eventExtensionRepository;
  private final InvoicePaymentCorrelationInfoRepository invoicePaymentCorrelationInfoRepository;
  private final InvoiceRepository invoiceRepository;
  private final ProductMasterExtensionRepository productMasterExtensionRepository;
  private final ProgramExtensionRepository programExtensionRepository;

  private final CorporateLoanMapper corporateLoanMapper;
  private final SinglePaymentMapper singlePaymentMapper;
  private final SingleElectronicPaymentService singleElectronicPaymentService;
  private final CorporateLoanService corporateLoanService;
  private final OperationalGatewayService operationalGatewayService;
  private final BusinessBankingService businessBankingService;

  /**
   * This function receives the financing result message from TI. First, it sends a notification to
   * the seller that the invoice has been financed, then it creates a credit and transfers the
   * disbursement amount from buyer to seller, the flows continues with a credit simulation (to know
   * how much taxes does the seller have to pay) and then a seller to buyer transaction is created,
   * then a notification is sent to the seller with a processed status and finally the result is
   * notified to business banking.
   *
   * @param serviceRequest The `TFBCFCRE` message.
   */
  @Override
  public void handleServiceRequest(AckServiceRequest<?> serviceRequest) {
    FinanceAckMessage financeMessage = (FinanceAckMessage) serviceRequest.getBody();
    String masterReference = financeMessage.getInvoiceArray().get(0).getInvoiceReference();

    log.info(
        "Starting financing flow for invoice [{}] with master ref [{}].",
        financeMessage.getTheirRef(),
        masterReference);

    InvoiceMaster invoice = findInvoiceByMasterReference(masterReference);

    try {
      Customer buyer = findCustomerByMnemonic(financeMessage.getBuyerIdentifier());
      Customer seller = findCustomerByMnemonic(financeMessage.getSellerIdentifier());
      ProductMasterExtension invoiceExtension = findMasterExtensionByReference(masterReference);
      ProgramExtension programExtension = findByProgrammeIdOrDefault(financeMessage.getProgramme());

      EncodedAccountParser buyerAccountParser =
          new EncodedAccountParser(invoiceExtension.getFinanceAccount());
      EncodedAccountParser sellerAccountParser = findSelectedSellerAccountOrDefault(financeMessage);

      sendEmailToCustomer(InvoiceEmailEvent.FINANCED, financeMessage, seller);
      DistributorCreditResponse buyerCredit =
          createBuyerCredit(financeMessage, programExtension, buyer, buyerAccountParser);
      saveGafOperationBuyerInformation(buyerCredit, invoiceExtension);
      SinglePaymentResponse response =
          transferCreditAmountFromBuyerToSeller(
              financeMessage, buyer, seller, buyerAccountParser, sellerAccountParser);
      saveInitialPaymentCorrelationInfo(response.data().paymentReferenceNumber(), financeMessage);
    } catch (Exception e) {
      handleFinanceFlowError(e, financeMessage, invoice);
    }
  }

  /**
   * This function is utilized by the payment result listener, which corresponds to the
   * buyer-to-seller transaction of the buyer's credit amount.
   *
   * @param financeMessage should be provided by the caller.
   * @param paymentResult should be received by the caller.
   * @param invoicePaymentInfo should be provided by the caller.
   * @throws CreditCreationException if the credit creation fails in GAF.
   * @throws InconsistentInvoiceInformationException if some information could not be found.
   */
  public void handleCreditPaymentResult(
      FinanceAckMessage financeMessage,
      PaymentResultResponse paymentResult,
      InvoicePaymentCorrelationInfo invoicePaymentInfo) {

    String invoiceReference = financeMessage.getInvoiceArray().get(0).getInvoiceReference();
    log.info("Started credit payment result handling for invoice [{}].", invoiceReference);

    try {
      validatePaymentResult(paymentResult);

      ProductMasterExtension invoiceExtension = findMasterExtensionByReference(invoiceReference);
      Customer buyer = findCustomerByMnemonic(financeMessage.getBuyerIdentifier());
      Customer seller = findCustomerByMnemonic(financeMessage.getSellerIdentifier());
      ProgramExtension programExtension = findByProgrammeIdOrDefault(financeMessage.getProgramme());
      EncodedAccountParser buyerAccountParser =
          new EncodedAccountParser(invoiceExtension.getFinanceAccount());
      EncodedAccountParser sellerAccountParser = findSelectedSellerAccountOrDefault(financeMessage);
      // Nueva funcion crear credi
      DistributorCreditResponse sellerCredit =
          simulateSellerCredit(financeMessage, buyer, programExtension, buyerAccountParser);
      saveGafOperationSellerInformation(sellerCredit, invoiceExtension);
      SinglePaymentResponse response =
          transferTaxesAmountFromSellerToBuyer(
              financeMessage, sellerCredit, buyer, seller, buyerAccountParser, sellerAccountParser);
      invoicePaymentInfo.setInitialEvent(
          InvoicePaymentCorrelationInfo.InitialEvent.BUYER_CENTRIC_FINANCE_1);
      invoicePaymentInfo.setPaymentReference(response.data().paymentReferenceNumber());

      invoicePaymentCorrelationInfoRepository.save(invoicePaymentInfo);
      log.info("Stored invoice correlation info with uuid: {}.", invoicePaymentInfo.getId());
    } catch (Exception e) {
      InvoiceMaster invoice = findInvoiceByMasterReference(invoiceReference);
      handleFinanceFlowError(e, financeMessage, invoice);
    }
  }

  /**
   * This method us used by the payment result listener, which corresponds to the seller-to-buyer
   * transaction of the seller's credit amount.
   *
   * @param financeMessage Finance message found in the correlation payload (redis).
   * @param paymentResult Payment result received from the FCM queue.
   * @param invoicePaymentCorrelationInfo Correlation info stored in redis.
   */
  public void handleTaxesPaymentResult(
      FinanceAckMessage financeMessage,
      PaymentResultResponse paymentResult,
      InvoicePaymentCorrelationInfo invoicePaymentCorrelationInfo) {
    String masterReference = financeMessage.getInvoiceArray().get(0).getInvoiceReference();
    InvoiceMaster invoice = findInvoiceByMasterReference(masterReference);
    log.info(
        "Started invoice taxes payment result handling for invoice [{}].",
        invoice.getReference().trim());

    try {
      validatePaymentResult(paymentResult);

      ProductMasterExtension invoiceExtension = findMasterExtensionByReference(masterReference);
      Customer seller = findCustomerByMnemonic(financeMessage.getSellerIdentifier());

      sendEmailToCustomer(InvoiceEmailEvent.PROCESSED, financeMessage, seller);
      notifyFinanceStatus(
          PayloadStatus.SUCCEEDED,
          financeMessage,
          invoice,
          invoiceExtension.getGafOperationId(),
          null);

      invoicePaymentCorrelationInfoRepository.delete(invoicePaymentCorrelationInfo);
    } catch (Exception e) {
      handleFinanceFlowError(e, financeMessage, invoice);
    }
  }

  private void validatePaymentResult(PaymentResultResponse paymentResult)
      throws SinglePaymentException {
    boolean paymentFailed = !PaymentResultResponse.Status.SUCCEEDED.equals(paymentResult.status());
    if (paymentFailed) {
      String paymentReference = paymentResult.paymentReference();
      String errorMessage =
          String.format(
              "Payment failed in buyer to seller transaction. Reference: [%s], type [%s].",
              paymentReference, paymentResult.type().value());

      invoicePaymentCorrelationInfoRepository.deleteByPaymentReference(paymentReference);

      throw new SinglePaymentException(errorMessage);
    }
  }

  private DistributorCreditResponse simulateSellerCredit(
      FinanceAckMessage financeMessage,
      Customer buyer,
      ProgramExtension programExtension,
      EncodedAccountParser buyerAccountParser) {
    log.info("Starting credit simulation.");
    int sellerCreditTerm = calculateCreditTermForSeller(financeMessage);
    DistributorCreditRequest creditRequest =
        corporateLoanMapper.mapToFinanceRequest(
            financeMessage, programExtension, buyer, buyerAccountParser, sellerCreditTerm);
    DistributorCreditResponse sellerCredit = corporateLoanService.simulateCredit(creditRequest);
    Error creditError = sellerCredit.data().error();

    boolean hasBeenCredited = creditError != null && creditError.hasNoError();
    if (!hasBeenCredited) {
      String creditErrorMessage =
          creditError != null ? creditError.message() : "Credit simulation failed.";
      throw new CreditCreationException(creditErrorMessage);
    }
    log.info("Starting seller to buyer taxes and solca transaction.");
    return sellerCredit;
  }

  private void handleFinanceFlowError(
      Throwable e, FinanceAckMessage financeMessage, InvoiceMaster invoice) {
    log.error(e.getMessage());

    boolean isNotifiableError =
        e instanceof CreditCreationException
            || e instanceof SinglePaymentException
            || e instanceof InconsistentInvoiceInformationException
            || e instanceof EncodedAccountParser.AccountDecodingException;
    if (!isNotifiableError) return;

    notifyFinanceStatus(PayloadStatus.FAILED, financeMessage, invoice, null, e.getMessage());
  }

  public void sendEmailToCustomer(
      InvoiceEmailEvent event, FinanceAckMessage financeMessage, Customer seller) {
    String invoiceNumber = financeMessage.getTheirRef().split("--")[0];

    BigDecimal financeDealAmount = getFinanceDealAmountFromMessage(financeMessage);

    InvoiceEmailInfo financedInvoiceInfo =
        InvoiceEmailInfo.builder()
            .customerMnemonic(financeMessage.getSellerIdentifier())
            .customerEmail(seller.getAddress().getCustomerEmail().trim())
            .customerName(seller.getFullName().trim())
            .date(financeMessage.getStartDate())
            .action(event.getValue())
            .invoiceCurrency(financeMessage.getFinanceDealCurrency())
            .invoiceNumber(invoiceNumber)
            .amount(financeDealAmount)
            .build();
    operationalGatewayService.sendNotificationRequest(financedInvoiceInfo);
  }

  private void notifyFinanceStatus(
      PayloadStatus status,
      FinanceAckMessage financeResultMessage,
      InvoiceMaster invoice,
      @Nullable String operationId,
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
                    .operationId(operationId)
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
                new InconsistentInvoiceInformationException(
                    "Could not find customer with mnemonic " + customerMnemonic));
  }

  private ProductMasterExtension findMasterExtensionByReference(String invoiceMasterReference) {
    return productMasterExtensionRepository
        .findByMasterReference(invoiceMasterReference)
        .orElseThrow(
            () ->
                new InconsistentInvoiceInformationException(
                    "Could not find account for the given invoice master."));
  }

  private InvoiceMaster findInvoiceByMasterReference(String invoiceMasterReference) {
    return invoiceRepository
        .findByProductMasterMasterReference(invoiceMasterReference)
        .orElseThrow(
            () ->
                new InconsistentInvoiceInformationException(
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
                new InconsistentInvoiceInformationException(
                    "Could not find account for customer " + customerMnemonic));
  }

  private DistributorCreditResponse createBuyerCredit(
      FinanceAckMessage financeMessage,
      ProgramExtension programExtension,
      Customer buyer,
      EncodedAccountParser buyerAccountParser)
      throws CreditCreationException {
    log.info("Starting credit creation.");
    int term = calculateCreditTermForBuyer(financeMessage, programExtension);
    DistributorCreditRequest credit =
        corporateLoanMapper.mapToFinanceRequest(
            financeMessage, programExtension, buyer, buyerAccountParser, term);
    DistributorCreditResponse buyerCredit = corporateLoanService.createCredit(credit);
    Error creditError = buyerCredit.data().error();

    boolean hasBeenCredited = creditError != null && creditError.hasNoError();
    if (!hasBeenCredited) {
      String creditErrorMessage =
          creditError != null ? creditError.message() : "Credit creation failed.";
      throw new CreditCreationException(creditErrorMessage);
    }
    return buyerCredit;
  }

  private SinglePaymentResponse transferCreditAmountFromBuyerToSeller(
      FinanceAckMessage financeMessage,
      Customer buyer,
      Customer seller,
      EncodedAccountParser buyerAccountParser,
      EncodedAccountParser sellerAccountParser) {
    log.info("Starting buyer to seller transaction.");
    String invoiceNumber = financeMessage.getTheirRef().split("--")[0];
    String debitDescription =
        String.format("Debito Fact %s %s", invoiceNumber, financeMessage.getSellerName());
    String creditDescription =
        String.format("Pago Fact %s %s", invoiceNumber, financeMessage.getBuyerName());

    SinglePaymentRequest buyerToSellerPayment =
        singlePaymentMapper.mapFinanceCustomerToCustomerTransaction(
            financeMessage,
            buyer,
            seller,
            buyerAccountParser,
            sellerAccountParser,
            debitDescription,
            creditDescription,
            getFinanceDealAmountFromMessage(financeMessage));

    return singleElectronicPaymentService.createSinglePayment(buyerToSellerPayment);
  }

  private SinglePaymentResponse transferTaxesAmountFromSellerToBuyer(
      FinanceAckMessage financeMessage,
      DistributorCreditResponse sellerCredit,
      Customer buyer,
      Customer seller,
      EncodedAccountParser buyerAccountParser,
      EncodedAccountParser sellerAccountParser) {
    log.info("Starting seller to buyer taxes transaction.");
    String invoiceNumber = financeMessage.getTheirRef().split("--")[0];
    String debitDescription =
        String.format("InteresySolca Fact %s %s", invoiceNumber, financeMessage.getBuyerName());
    String creditDescription =
        String.format("InteresySolca Fact %s %s", invoiceNumber, financeMessage.getSellerName());

    BigDecimal amount = calculateSolcaAndTaxesFromCreditResponse(sellerCredit, financeMessage);
    SinglePaymentRequest buyerToSellerPayment =
        singlePaymentMapper.mapFinanceCustomerToCustomerTransaction(
            financeMessage,
            seller,
            buyer,
            sellerAccountParser,
            buyerAccountParser,
            debitDescription,
            creditDescription,
            amount);

    return singleElectronicPaymentService.createSinglePayment(buyerToSellerPayment);
  }

  private void saveInitialPaymentCorrelationInfo(
      String paymentReference, FinanceAckMessage financeMessage) {
    try {
      InvoicePaymentCorrelationInfo info =
          InvoicePaymentCorrelationInfo.builder()
              .id(uuidGenerator.getNewId())
              .paymentReference(paymentReference)
              .initialEvent(InvoicePaymentCorrelationInfo.InitialEvent.BUYER_CENTRIC_FINANCE_0)
              .eventPayload(objectMapper.writeValueAsString(financeMessage))
              .build();
      invoicePaymentCorrelationInfoRepository.save(info);

      log.info("Payment correlation info saved. Id={}", info.getId());
    } catch (JsonProcessingException e) {
      throw new InconsistentInvoiceInformationException(e.getMessage());
    }
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

  private void saveGafOperationBuyerInformation(
      DistributorCreditResponse creditResponse, ProductMasterExtension invoiceExtension) {
    BigDecimal buyerGafInterests = getInterests(creditResponse);
    Data credit = creditResponse.data();

    invoiceExtension.setGafOperationId(credit.operationId());
    invoiceExtension.setGafInterestRate(BigDecimal.valueOf(credit.interestRate()));
    invoiceExtension.setGafDisbursementAmount(BigDecimal.valueOf(credit.disbursementAmount()));
    invoiceExtension.setGafTaxFactor(BigDecimal.valueOf(credit.tax().factor()));
    invoiceExtension.setBuyerGafInterests(buyerGafInterests);
    invoiceExtension.setBuyerSolcaAmount(BigDecimal.valueOf(credit.tax().amount()));
    invoiceExtension.setAmortizations(getAmortizationsPayload(creditResponse));

    productMasterExtensionRepository.save(invoiceExtension);

    log.info("Saved GAF information for invoice extension with id [{}].", invoiceExtension.getId());
  }

  private void saveGafOperationSellerInformation(
      DistributorCreditResponse creditResponse, ProductMasterExtension invoiceExtension) {
    Data credit = creditResponse.data();
    BigDecimal sellerGafInterests = getInterests(creditResponse);

    invoiceExtension.setSellerGafInterests(sellerGafInterests);
    invoiceExtension.setSellerSolcaAmount(BigDecimal.valueOf(credit.tax().amount()));

    productMasterExtensionRepository.save(invoiceExtension);
    log.info(
        "Saved GAF information for seller on invoice extension with id [{}].",
        invoiceExtension.getId());
  }

  private BigDecimal getInterests(DistributorCreditResponse creditResponse) {
    return creditResponse.data().amortizations().stream()
        .filter(a -> "IV".equals(a.code()))
        .findFirst()
        .map(Amortization::amount)
        .map(BigDecimal::new)
        .orElse(BigDecimal.ZERO);
  }

  private String getAmortizationsPayload(DistributorCreditResponse creditResponse) {
    try {
      return objectMapper.writeValueAsString(creditResponse.data().amortizations());
    } catch (JsonProcessingException e) {
      log.error(e.getMessage());
      return "";
    }
  }
}
