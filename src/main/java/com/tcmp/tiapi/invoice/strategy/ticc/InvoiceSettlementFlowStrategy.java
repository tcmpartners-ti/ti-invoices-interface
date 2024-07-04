package com.tcmp.tiapi.invoice.strategy.ticc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcmp.tiapi.customer.model.Account;
import com.tcmp.tiapi.customer.model.Customer;
import com.tcmp.tiapi.customer.repository.AccountRepository;
import com.tcmp.tiapi.customer.repository.CustomerRepository;
import com.tcmp.tiapi.invoice.dto.InvoiceRealOutputData;
import com.tcmp.tiapi.invoice.dto.ti.settle.InvoiceSettlementEventMessage;
import com.tcmp.tiapi.invoice.model.InvoiceMaster;
import com.tcmp.tiapi.invoice.model.ProductMasterExtension;
import com.tcmp.tiapi.invoice.model.bulkcreate.BulkCreateInvoicesFileInfo;
import com.tcmp.tiapi.invoice.repository.InvoiceRepository;
import com.tcmp.tiapi.invoice.repository.ProductMasterExtensionRepository;
import com.tcmp.tiapi.invoice.repository.redis.BulkCreateInvoicesFileInfoRepository;
import com.tcmp.tiapi.invoice.service.files.realoutput.InvoiceRealOutputFileUploader;
import com.tcmp.tiapi.invoice.util.EncodedAccountParser;
import com.tcmp.tiapi.program.model.ProgramExtension;
import com.tcmp.tiapi.program.repository.ProgramExtensionRepository;
import com.tcmp.tiapi.shared.ApplicationEnv;
import com.tcmp.tiapi.shared.UUIDGenerator;
import com.tcmp.tiapi.shared.utils.MonetaryAmountUtils;
import com.tcmp.tiapi.ti.dto.request.AckServiceRequest;
import com.tcmp.tiapi.ti.route.ticc.TICCIncomingStrategy;
import com.tcmp.tiapi.titoapigee.businessbanking.BusinessBankingService;
import com.tcmp.tiapi.titoapigee.businessbanking.dto.request.*;
import com.tcmp.tiapi.titoapigee.businessbanking.model.OperationalGatewayProcessCode;
import com.tcmp.tiapi.titoapigee.corporateloan.CorporateLoanService;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.CorporateLoanMapper;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.request.*;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.response.*;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.response.Error;
import com.tcmp.tiapi.titoapigee.corporateloan.exception.CreditCreationException;
import com.tcmp.tiapi.titoapigee.operationalgateway.OperationalGatewayService;
import com.tcmp.tiapi.titoapigee.operationalgateway.model.InvoiceEmailEvent;
import com.tcmp.tiapi.titoapigee.operationalgateway.model.InvoiceEmailInfo;
import com.tcmp.tiapi.titofcm.dto.SinglePaymentMapper;
import com.tcmp.tiapi.titofcm.dto.request.SinglePaymentRequest;
import com.tcmp.tiapi.titofcm.dto.response.PaymentResultResponse;
import com.tcmp.tiapi.titofcm.exception.SinglePaymentException;
import com.tcmp.tiapi.titofcm.model.InvoicePaymentCorrelationInfo;
import com.tcmp.tiapi.titofcm.repository.InvoicePaymentCorrelationInfoRepository;
import com.tcmp.tiapi.titofcm.service.SingleElectronicPaymentService;
import jakarta.annotation.Nullable;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple5;

@Component
@RequiredArgsConstructor
@Slf4j
public class InvoiceSettlementFlowStrategy implements TICCIncomingStrategy {
  private final UUIDGenerator uuidGenerator;
  private final ObjectMapper objectMapper;
  private final Clock clock;

  private final CorporateLoanService corporateLoanService;
  private final OperationalGatewayService operationalGatewayService;
  private final BusinessBankingService businessBankingService;
  private final SingleElectronicPaymentService singleElectronicPaymentService;

  private final AccountRepository accountRepository;
  private final CustomerRepository customerRepository;
  private final InvoicePaymentCorrelationInfoRepository invoicePaymentCorrelationInfoRepository;
  private final InvoiceRepository invoiceRepository;
  private final BulkCreateInvoicesFileInfoRepository createInvoicesFileInfoRepository;
  private final InvoiceRealOutputFileUploader realOutputFileUploader;
  private final ProductMasterExtensionRepository productMasterExtensionRepository;
  private final ProgramExtensionRepository programExtensionRepository;
  private final SinglePaymentMapper singlePaymentMapper;
  private final CorporateLoanMapper corporateLoanMapper;

  @Value("${spring.profiles.active}")
  private String activeProfile;

  /**
   * This function handles the settlement message sent by TI. It notifies to Business Banking the
   * status of the settlement
   *
   * @param serviceRequest The `TFINVSETCU` message (custom).
   */
  @Override
  public void handleServiceRequest(AckServiceRequest<?> serviceRequest) {
    InvoiceSettlementEventMessage message =
        (InvoiceSettlementEventMessage) serviceRequest.getBody();
    log.info(
        "Started settlement flow for invoice [{}], with master ref [{}].",
        message.getInvoiceNumber(),
        message.getMasterRef());

    InvoiceMaster invoice = findInvoiceByMasterReference(message.getMasterRef());
    ProductMasterExtension invoiceExtension =
        findMasterExtensionByReference(message.getMasterRef());
    invoice.setProductMasterExtension(invoiceExtension);

    Mono.zip(
            Mono.fromCallable(() -> findCustomerByMnemonic(message.getBuyerIdentifier())),
            Mono.fromCallable(() -> findCustomerByMnemonic(message.getSellerIdentifier())),
            Mono.fromCallable(() -> findProgrammeExtensionByIdOrDefault(message.getProgramme())),
            Mono.fromCallable(() -> invoiceExtension),
            Mono.fromCallable(() -> findAccountByCustomerMnemonic(message.getSellerIdentifier())))
        .flatMap(
            tuple -> {
              ProgramExtension programExtension = tuple.getT3();

              boolean hasExtraFinancingDays = programExtension.getExtraFinancingDays() > 0;
              if (!hasExtraFinancingDays) return handleNoExtraDaysInvoice();

              boolean hasBeenFinanced = invoiceHasLinkedFinanceEvent(invoice);
              if (hasBeenFinanced) return handleFinancedInvoice(message, tuple);

              return handleNotFinancedInvoice(message, tuple);
            })
        .doOnSuccess(success -> log.info("Invoice settlement flow completed."))
        .onErrorResume(error -> handleError(error, message, invoice))
        .subscribe();
  }

  private Mono<Object> handleNoExtraDaysInvoice() {
    // For Mvp, we ignore invoices with no extra financing days.
    log.info("Programe has no extra financing days, flow ended.");
    return Mono.empty();
  }

  private Mono<Object> handleFinancedInvoice(
      InvoiceSettlementEventMessage message,
      Tuple5<Customer, Customer, ProgramExtension, ProductMasterExtension, Account> tuple) {
    Customer buyer = tuple.getT1();
    return sendEmailToCustomer(InvoiceEmailEvent.CREDITED, message, buyer);
  }

  private Mono<Object> handleNotFinancedInvoice(
      InvoiceSettlementEventMessage message,
      Tuple5<Customer, Customer, ProgramExtension, ProductMasterExtension, Account> tuple) {
    Customer buyer = tuple.getT1();
    Customer seller = tuple.getT2();
    ProgramExtension programExtension = tuple.getT3();

    EncodedAccountParser buyerAccount = new EncodedAccountParser(tuple.getT4().getFinanceAccount());
    EncodedAccountParser sellerAccount =
        new EncodedAccountParser(tuple.getT5().getExternalAccountNumber());

    return sendEmailToCustomer(InvoiceEmailEvent.SETTLED, message, seller)
        .then(
            createBuyerCredit(message, buyer, programExtension, buyerAccount)
                .flatMap(creditResponse -> saveGafInformation(creditResponse, tuple.getT4())))
        .then(sendEmailToCustomer(InvoiceEmailEvent.CREDITED, message, buyer))
        .then(
            transferPaymentAmountFromBuyerToSeller(
                    message, buyer, seller, buyerAccount, sellerAccount)
                .map(this::saveTransactionCorrelationInfo));
  }

  /**
   * This function stores required invoice info to be processed when the payment result is received
   * in the queue (TRADE_INNOVATION_NOT).
   *
   * @param tuple Correlation information.
   * @return Empty mono if data was saved successfully.
   */
  private Mono<Object> saveTransactionCorrelationInfo(
      Tuple2<String, InvoiceSettlementEventMessage> tuple) {
    try {
      InvoiceSettlementEventMessage message = tuple.getT2();
      String eventPayload = objectMapper.writeValueAsString(message);

      InvoicePaymentCorrelationInfo invoicePaymentCorrelationInfo =
          InvoicePaymentCorrelationInfo.builder()
              .id(uuidGenerator.getNewId())
              .paymentReference(tuple.getT1())
              .initialEvent(InvoicePaymentCorrelationInfo.InitialEvent.SETTLEMENT)
              .eventPayload(eventPayload)
              .build();

      invoicePaymentCorrelationInfoRepository.save(invoicePaymentCorrelationInfo);
      log.info("Payment correlation info saved. Id={}", invoicePaymentCorrelationInfo.getId());

      return Mono.empty();
    } catch (JsonProcessingException e) {
      return Mono.error(e);
    }
  }

  /**
   * This function handles the transaction payment result for the credit amount. The result is
   * received from FCM.
   *
   * @see
   *     com.tcmp.tiapi.invoice.strategy.payment.SettlementPaymentResultStrategy#handleResult(InvoicePaymentCorrelationInfo,
   *     PaymentResultResponse) where the method is consumed from.
   * @param message message content found in redis payload.
   * @param paymentResultResponse the payment execution result.
   * @return mono response with the result of the final step of settlement flow.
   */
  public Mono<Object> handleTransactionPaymentResult(
      InvoiceSettlementEventMessage message, PaymentResultResponse paymentResultResponse) {

    String masterReference = message.getMasterRef();
    InvoiceMaster invoice = findInvoiceByMasterReference(masterReference);
    ProductMasterExtension invoiceExtension = findMasterExtensionByReference(masterReference);
    invoice.setProductMasterExtension(invoiceExtension);

    Customer seller = findCustomerByMnemonic(message.getSellerIdentifier());

    String fileUuid = invoiceExtension.getFileCreationUuid();
    boolean createdViaSftp = fileUuid != null && !fileUuid.isBlank();
    boolean transactionFailed =
        !PaymentResultResponse.Status.SUCCEEDED.equals(paymentResultResponse.status());
    if (transactionFailed) {
      SinglePaymentException error =
          new SinglePaymentException("Could not perform bgl to seller transaction");
      return handleError(error, message, invoice);
    }

    if (createdViaSftp) {
      return notifyStatusViaSftp(InvoiceRealOutputData.Status.PAID, message, invoice)
          .then(
              notifyStatusViaBusinessBanking(
                  PayloadStatus.SUCCEEDED,
                  OperationalGatewayProcessCode.INVOICE_SETTLEMENT_SFTP,
                  message,
                  invoice,
                  invoiceExtension.getGafOperationId(),
                  null));
    }

    // Credit received, should be all good
    return sendEmailToCustomer(InvoiceEmailEvent.PROCESSED, message, seller)
        .then(
            notifyStatusViaBusinessBanking(
                PayloadStatus.SUCCEEDED,
                OperationalGatewayProcessCode.INVOICE_SETTLEMENT,
                message,
                invoice,
                invoiceExtension.getGafOperationId(),
                null));
  }

  private Mono<Object> sendEmailToCustomer(
      InvoiceEmailEvent event, InvoiceSettlementEventMessage message, Customer customer) {

    String invoiceNumber = message.getInvoiceNumber().split("--")[0];
    BigDecimal paymentAmount = getPaymentAmountFromMessage(message);

    InvoiceEmailInfo creditedInvoiceInfo =
        InvoiceEmailInfo.builder()
            .customerMnemonic(message.getBuyerIdentifier())
            .customerEmail(customer.getAddress().getCustomerEmail().trim())
            .customerName(customer.getFullName().trim())
            .date(message.getPaymentValueDate())
            .action(event.getValue())
            .invoiceCurrency(message.getPaymentCurrency().trim())
            .invoiceNumber(invoiceNumber)
            .amount(paymentAmount)
            .build();

    return Mono.fromRunnable(
            () -> operationalGatewayService.sendNotificationRequest(creditedInvoiceInfo))
        // Keep going if email could not be sent
        .onErrorResume(
            e -> {
              log.error(e.getMessage());
              return Mono.empty();
            });
  }

  private Mono<Object> handleError(
      Throwable e, InvoiceSettlementEventMessage message, InvoiceMaster invoice) {
    log.error(e.getMessage());

    boolean isNotifiableError =
        e instanceof CreditCreationException || e instanceof SinglePaymentException;
    if (!isNotifiableError) return Mono.empty();

    String fileUuid = invoice.getProductMasterExtension().getFileCreationUuid();
    boolean createdViaSftp = fileUuid != null && !fileUuid.isBlank();
    if (createdViaSftp) {
      return notifyStatusViaSftp(InvoiceRealOutputData.Status.FAILED, message, invoice)
          .then(
              notifyStatusViaBusinessBanking(
                  PayloadStatus.FAILED,
                  OperationalGatewayProcessCode.INVOICE_SETTLEMENT_SFTP,
                  message,
                  invoice,
                  null,
                  e.getMessage()));
    }

    return notifyStatusViaBusinessBanking(
        PayloadStatus.FAILED,
        OperationalGatewayProcessCode.INVOICE_SETTLEMENT,
        message,
        invoice,
        null,
        e.getMessage());
  }

  private Mono<Object> notifyStatusViaBusinessBanking(
      PayloadStatus status,
      OperationalGatewayProcessCode processCode,
      InvoiceSettlementEventMessage message,
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
                    .reference(message.getInvoiceNumber())
                    .sellerMnemonic(message.getSellerIdentifier())
                    .operationId(operationId)
                    .build())
            .details(new PayloadDetails(errors, null, null))
            .build();

    return Mono.fromRunnable(() -> businessBankingService.notifyEvent(processCode, payload));
  }

  private Mono<Object> notifyStatusViaSftp(
      InvoiceRealOutputData.Status status,
      InvoiceSettlementEventMessage message,
      InvoiceMaster invoice) {
    String fileUuid = invoice.getProductMasterExtension().getFileCreationUuid().trim();

    BulkCreateInvoicesFileInfo fileInfo =
        createInvoicesFileInfoRepository
            .findById(fileUuid)
            .orElseThrow(
                () ->
                    new EntityNotFoundException("Could not find file info with uuid " + fileUuid));

    String filename = fileInfo.getOriginalFilename();
    InvoiceRealOutputData realOutputRow =
        InvoiceRealOutputData.builder()
            .invoiceReference(invoice.getReference().trim())
            .processedAt(LocalDateTime.now(clock))
            .status(status)
            .amount(getPaymentAmountFromMessage(message))
            .counterPartyMnemonic(message.getSellerIdentifier())
            .build();

    return Mono.fromRunnable(
        () -> realOutputFileUploader.appendInvoiceStatusRow(filename, realOutputRow));
  }

  private InvoiceMaster findInvoiceByMasterReference(String masterReference) {
    return invoiceRepository
        .findByProductMasterMasterReference(masterReference)
        .orElseThrow(() -> new EntityNotFoundException("Could not find invoice."));
  }

  private Customer findCustomerByMnemonic(String customerMnemonic) {
    return customerRepository
        .findFirstByIdMnemonic(customerMnemonic)
        .orElseThrow(
            () ->
                new EntityNotFoundException(
                    "Could not find customer with mnemonic " + customerMnemonic));
  }

  private ProductMasterExtension findMasterExtensionByReference(String masterReference) {
    return productMasterExtensionRepository
        .findByMasterReference(masterReference)
        .orElseThrow(
            () ->
                new EntityNotFoundException(
                    "Could not find account for the given invoice master."));
  }

  private Account findAccountByCustomerMnemonic(String customerMnemonic) {
    return accountRepository
        .findByTypeAndCustomerMnemonic("CA", customerMnemonic)
        .orElseThrow(
            () ->
                new EntityNotFoundException(
                    "Could not find account for seller " + customerMnemonic));
  }

  private ProgramExtension findProgrammeExtensionByIdOrDefault(String programmeId) {
    return programExtensionRepository
        .findByProgrammeId(programmeId)
        .orElse(
            ProgramExtension.builder()
                .programmeId(programmeId)
                .extraFinancingDays(0)
                .requiresExtraFinancing(false)
                .build());
  }

  private boolean invoiceHasLinkedFinanceEvent(InvoiceMaster invoice) {
    BigDecimal discountDealAmount = invoice.getDiscountDealAmount();

    return !invoice.getIsDrawDownEligible()
        && invoice.getCreateFinanceEventId() != null
        && (discountDealAmount != null && BigDecimal.ZERO.compareTo(discountDealAmount) != 0);
  }

  private Mono<DistributorCreditResponse> createBuyerCredit(
      InvoiceSettlementEventMessage message,
      Customer buyer,
      ProgramExtension programExtension,
      EncodedAccountParser buyerAccount)
      throws CreditCreationException {
    return Mono.fromCallable(
            () -> {
              log.info("Started credit creation.");
              DistributorCreditRequest request =
                  buildDistributorCreditRequest(message, buyer, programExtension, buyerAccount);
              return corporateLoanService.createCredit(request);
            })
        .flatMap(
            creditResponse -> {
              Error error = creditResponse.data().error();
              boolean hasBeenCredited = error != null && error.hasNoError();
              if (!hasBeenCredited) {
                String creditErrorMessage =
                    error != null ? error.message() : "Credit creation failed.";

                return Mono.error(new CreditCreationException(creditErrorMessage));
              }

              return Mono.just(creditResponse);
            });
  }

  private Mono<ProductMasterExtension> saveGafInformation(
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

    return Mono.fromRunnable(
        () -> {
          productMasterExtensionRepository.save(invoiceExtension);

          log.info(
              "Saved GAF information for invoice extension with id [{}].",
              invoiceExtension.getId());
        });
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

  private DistributorCreditRequest buildDistributorCreditRequest(
      InvoiceSettlementEventMessage invoiceSettlementMessage,
      Customer buyer,
      ProgramExtension programExtension,
      EncodedAccountParser buyerAccountParser) {
    // Mock this value for dev testing purposes.
    boolean isDev =
        ApplicationEnv.LOCAL.value().equals(activeProfile)
            || ApplicationEnv.DEV.value().equals(activeProfile);
    String paymentValueDate =
        isDev ? getSystemDate() : invoiceSettlementMessage.getPaymentValueDate();

    return corporateLoanMapper.mapToFinanceRequest(
        invoiceSettlementMessage, buyer, buyerAccountParser, paymentValueDate, programExtension);
  }

  private String getSystemDate() {
    LocalDate currentDate = LocalDate.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    return currentDate.format(formatter);
  }

  private Mono<Tuple2<String, InvoiceSettlementEventMessage>>
      transferPaymentAmountFromBuyerToSeller(
          InvoiceSettlementEventMessage message,
          Customer buyer,
          Customer seller,
          EncodedAccountParser buyerAccount,
          EncodedAccountParser sellerAccount)
          throws SinglePaymentException {
    log.info("Started buyer to seller transaction.");

    String invoiceReference = message.getInvoiceNumber().split("--")[0];
    String debitConcept =
        String.format("Debito Fact %s %s", invoiceReference, seller.getFullName().trim());
    String creditConcept =
        String.format("Pago Fact %s %s", invoiceReference, buyer.getFullName().trim());

    SinglePaymentRequest creditAndDebitRequest =
        singlePaymentMapper.mapSettlementCustomerToCustomerTransaction(
            message, buyer, seller, buyerAccount, sellerAccount, debitConcept, creditConcept);

    return Mono.fromCallable(
            () -> singleElectronicPaymentService.createSinglePayment(creditAndDebitRequest))
        .flatMap(
            singlePaymentResponse -> {
              String creditAndDebitReference =
                  singlePaymentResponse.data().paymentReferenceNumber();
              log.info("Credit And Debit Reference: {}", creditAndDebitReference);

              return Mono.zip(Mono.just(creditAndDebitReference), Mono.just(message));
            });
  }

  private BigDecimal getPaymentAmountFromMessage(
      InvoiceSettlementEventMessage invoiceSettlementMessage) {
    BigDecimal paymentAmountInCents = new BigDecimal(invoiceSettlementMessage.getPaymentAmount());
    return MonetaryAmountUtils.convertCentsToDollars(paymentAmountInCents);
  }
}
