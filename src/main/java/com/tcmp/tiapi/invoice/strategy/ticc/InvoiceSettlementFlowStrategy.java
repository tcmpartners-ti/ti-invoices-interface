package com.tcmp.tiapi.invoice.strategy.ticc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcmp.tiapi.customer.model.Account;
import com.tcmp.tiapi.customer.model.Customer;
import com.tcmp.tiapi.customer.repository.AccountRepository;
import com.tcmp.tiapi.customer.repository.CustomerRepository;
import com.tcmp.tiapi.invoice.dto.ti.settle.InvoiceSettlementEventMessage;
import com.tcmp.tiapi.invoice.model.InvoiceMaster;
import com.tcmp.tiapi.invoice.model.ProductMasterExtension;
import com.tcmp.tiapi.invoice.repository.InvoiceRepository;
import com.tcmp.tiapi.invoice.repository.ProductMasterExtensionRepository;
import com.tcmp.tiapi.invoice.strategy.payment.SettlementPaymentResultStrategy;
import com.tcmp.tiapi.invoice.util.EncodedAccountParser;
import com.tcmp.tiapi.program.model.ProgramExtension;
import com.tcmp.tiapi.program.repository.ProgramExtensionRepository;
import com.tcmp.tiapi.shared.ApplicationEnv;
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
import com.tcmp.tiapi.titoapigee.corporateloan.dto.request.*;
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
import java.time.LocalDate;
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

  private final CorporateLoanService corporateLoanService;
  private final OperationalGatewayService operationalGatewayService;
  private final BusinessBankingService businessBankingService;
  private final SingleElectronicPaymentService singleElectronicPaymentService;

  private final AccountRepository accountRepository;
  private final CustomerRepository customerRepository;
  private final InvoicePaymentCorrelationInfoRepository invoicePaymentCorrelationInfoRepository;
  private final InvoiceRepository invoiceRepository;
  private final ProductMasterExtensionRepository productMasterExtensionRepository;
  private final ProgramExtensionRepository programExtensionRepository;
  private final SinglePaymentMapper singlePaymentMapper;

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

    Mono.zip(
            Mono.fromCallable(() -> findCustomerByMnemonic(message.getBuyerIdentifier())),
            Mono.fromCallable(() -> findCustomerByMnemonic(message.getSellerIdentifier())),
            Mono.fromCallable(() -> findProgrammeExtensionByIdOrDefault(message.getProgramme())),
            Mono.fromCallable(() -> findMasterExtensionByReference(message.getMasterRef())),
            Mono.fromCallable(() -> findAccountByCustomerMnemonic(message.getSellerIdentifier())))
        .flatMap(
            tuple -> {
              log.info("Started settlement flow.");
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
        .then(createBuyerCredit(message, buyer, programExtension, buyerAccount))
        .then(sendEmailToCustomer(InvoiceEmailEvent.CREDITED, message, buyer))
        .then(
            transferPaymentAmountFromBuyerToSeller(
                    message, buyer, seller, buyerAccount, sellerAccount)
                .map(this::handleBuyerToSellerTransaction));
  }

  /**
   * This function stores required invoice info to be processed when the payment result is received
   * in the queue (TRADE_INNOVATION_NOT).
   *
   * @see SettlementPaymentResultStrategy#handleResult(InvoicePaymentCorrelationInfo,
   *     PaymentResultResponse)
   * @param tuple Correlation information.
   * @return Empty mono if data was saved successfully.
   */
  private Mono<Object> handleBuyerToSellerTransaction(
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

  public Mono<Object> sendEmailToCustomer(
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

  public Mono<Object> handleError(
      Throwable e, InvoiceSettlementEventMessage message, InvoiceMaster invoice) {
    log.error(e.getMessage());

    boolean isNotifiableError =
        e instanceof CreditCreationException || e instanceof SinglePaymentException;
    if (!isNotifiableError) return Mono.empty();

    return notifySettlementStatusExternally(PayloadStatus.FAILED, message, invoice, e.getMessage());
  }

  public Mono<Object> notifySettlementStatusExternally(
      PayloadStatus status,
      InvoiceSettlementEventMessage message,
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

    return Mono.fromRunnable(
        () ->
            businessBankingService.notifyEvent(
                OperationalGatewayProcessCode.INVOICE_SETTLEMENT, payload));
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

  private Mono<Object> createBuyerCredit(
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

              return Mono.empty();
            });
  }

  private DistributorCreditRequest buildDistributorCreditRequest(
      InvoiceSettlementEventMessage invoiceSettlementMessage,
      Customer buyer,
      ProgramExtension programExtension,
      EncodedAccountParser buyerAccountParser) {
    // Mock this value for dev testing purposes.
    boolean isDevelopment =
        ApplicationEnv.LOCAL.value().equals(activeProfile)
            || ApplicationEnv.DEV.value().equals(activeProfile);
    String paymentValueDate =
        isDevelopment ? getSystemDate() : invoiceSettlementMessage.getPaymentValueDate();

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
        .amount(getPaymentAmountFromMessage(invoiceSettlementMessage))
        .effectiveDate(paymentValueDate)
        .term(programExtension.getExtraFinancingDays())
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
        String.format("Descuento Factura %s %s", invoiceReference, seller.getFullName().trim());
    String creditConcept =
        String.format("Pago Factura %s %s", invoiceReference, buyer.getFullName().trim());

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
