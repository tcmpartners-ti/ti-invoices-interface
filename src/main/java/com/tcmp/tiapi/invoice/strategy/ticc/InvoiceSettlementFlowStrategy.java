package com.tcmp.tiapi.invoice.strategy.ticc;

import com.tcmp.tiapi.customer.model.Account;
import com.tcmp.tiapi.customer.model.Customer;
import com.tcmp.tiapi.customer.repository.AccountRepository;
import com.tcmp.tiapi.customer.repository.CustomerRepository;
import com.tcmp.tiapi.invoice.dto.ti.settle.InvoiceSettlementEventMessage;
import com.tcmp.tiapi.invoice.model.InvoiceMaster;
import com.tcmp.tiapi.invoice.model.ProductMasterExtension;
import com.tcmp.tiapi.invoice.repository.InvoiceRepository;
import com.tcmp.tiapi.invoice.repository.ProductMasterExtensionRepository;
import com.tcmp.tiapi.invoice.util.EncodedAccountParser;
import com.tcmp.tiapi.program.model.ProgramExtension;
import com.tcmp.tiapi.program.repository.ProgramExtensionRepository;
import com.tcmp.tiapi.shared.ApplicationEnv;
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
import com.tcmp.tiapi.titoapigee.corporateloan.dto.response.Error;
import com.tcmp.tiapi.titoapigee.corporateloan.exception.CreditCreationException;
import com.tcmp.tiapi.titoapigee.operationalgateway.OperationalGatewayService;
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
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple5;

@Component
@RequiredArgsConstructor
@Slf4j
public class InvoiceSettlementFlowStrategy implements TICCIncomingStrategy {
  private final CorporateLoanService corporateLoanService;
  private final PaymentExecutionService paymentExecutionService;
  private final OperationalGatewayService operationalGatewayService;
  private final BusinessBankingService businessBankingService;

  private final AccountRepository accountRepository;
  private final CustomerRepository customerRepository;
  private final InvoiceRepository invoiceRepository;
  private final ProductMasterExtensionRepository productMasterExtensionRepository;
  private final ProgramExtensionRepository programExtensionRepository;

  @Value("${spring.profiles.active}")
  private String activeProfile;

  @Value("${bp.service.payment-execution.bgl-account}")
  private String bglAccount;

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

              return handleNotFinancedInvoice(message, tuple, invoice);
            })
        .doOnSuccess(success -> log.info("Invoice settlement flow completed successfully."))
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

    BigDecimal paymentAmountInCents = new BigDecimal(message.getPaymentAmount());
    BigDecimal paymentAmount = MonetaryAmountUtils.convertCentsToDollars(paymentAmountInCents);

    return sendEmailToCustomer(InvoiceEmailEvent.CREDITED, message, buyer, paymentAmount);
  }

  private Mono<Object> handleNotFinancedInvoice(
      InvoiceSettlementEventMessage message,
      Tuple5<Customer, Customer, ProgramExtension, ProductMasterExtension, Account> tuple,
      InvoiceMaster invoice) {
    Customer buyer = tuple.getT1();
    Customer seller = tuple.getT2();
    ProgramExtension programExtension = tuple.getT3();

    EncodedAccountParser buyerAccount = new EncodedAccountParser(tuple.getT4().getFinanceAccount());
    EncodedAccountParser sellerAccount =
        new EncodedAccountParser(tuple.getT5().getExternalAccountNumber());

    BigDecimal paymentAmountInCents = new BigDecimal(message.getPaymentAmount());
    BigDecimal paymentAmount = MonetaryAmountUtils.convertCentsToDollars(paymentAmountInCents);

    return sendEmailToCustomer(InvoiceEmailEvent.SETTLED, message, seller, paymentAmount)
        .then(createBuyerCredit(message, buyer, programExtension, buyerAccount))
        .then(sendEmailToCustomer(InvoiceEmailEvent.CREDITED, message, buyer, paymentAmount))
        .then(
            transferPaymentAmountFromBuyerToSeller(
                message, buyer, seller, buyerAccount, sellerAccount))
        .then(sendEmailToCustomer(InvoiceEmailEvent.PROCESSED, message, seller, paymentAmount))
        .then(notifySettlementStatusExternally(PayloadStatus.SUCCEEDED, message, invoice, null));
  }

  private Mono<Object> sendEmailToCustomer(
      InvoiceEmailEvent event,
      InvoiceSettlementEventMessage message,
      Customer customer,
      BigDecimal paymentAmount) {

    InvoiceEmailInfo creditedInvoiceInfo =
        InvoiceEmailInfo.builder()
            .customerMnemonic(message.getBuyerIdentifier())
            .customerEmail(customer.getAddress().getCustomerEmail().trim())
            .customerName(customer.getFullName().trim())
            .date(message.getPaymentValueDate())
            .action(event.getValue())
            .invoiceCurrency(message.getPaymentCurrency().trim())
            .invoiceNumber(message.getInvoiceNumber())
            .amount(paymentAmount)
            .build();

    return Mono.fromRunnable(
        () -> operationalGatewayService.sendNotificationRequest(creditedInvoiceInfo));
  }

  private Mono<Object> handleError(
      Throwable e, InvoiceSettlementEventMessage message, InvoiceMaster invoice) {
    log.error(e.getMessage());

    boolean isNotifiableError =
        e instanceof CreditCreationException || e instanceof PaymentExecutionException;
    if (!isNotifiableError) return Mono.empty();

    return notifySettlementStatusExternally(PayloadStatus.FAILED, message, invoice, e.getMessage());
  }

  private Mono<Object> notifySettlementStatusExternally(
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

  private Mono<Object> transferPaymentAmountFromBuyerToSeller(
      InvoiceSettlementEventMessage message,
      Customer buyer,
      Customer seller,
      EncodedAccountParser buyerAccountParser,
      EncodedAccountParser sellerAccountParser)
      throws PaymentExecutionException {
    log.info("Started buyer to seller transaction.");

    String invoiceReference = message.getInvoiceNumber();
    String debitConcept =
        String.format("Descuento Factura %s %s", invoiceReference, seller.getFullName().trim());
    String creditConcept =
        String.format("Pago Factura %s %s", invoiceReference, buyer.getFullName().trim());
    String currency = message.getPaymentCurrency();
    BigDecimal amount = getPaymentAmountFromMessage(message);

    TransactionRequest debitRequest =
        TransactionRequest.from(
            TransactionType.CLIENT_TO_BGL,
            buyerAccountParser.getAccount(),
            bglAccount,
            debitConcept,
            currency,
            amount);
    TransactionRequest creditRequest =
        TransactionRequest.from(
            TransactionType.BGL_TO_CLIENT,
            bglAccount,
            sellerAccountParser.getAccount(),
            creditConcept,
            currency,
            amount);

    return Mono.zip(
            Mono.fromCallable(() -> paymentExecutionService.makeTransactionRequest(debitRequest)),
            Mono.fromCallable(() -> paymentExecutionService.makeTransactionRequest(creditRequest)))
        .flatMap(
            tuple -> {
              BusinessAccountTransfersResponse buyerToBglTransfer = tuple.getT1();
              BusinessAccountTransfersResponse bglToSellerTransfer = tuple.getT2();

              boolean isTransactionOk = buyerToBglTransfer.isOk() && bglToSellerTransfer.isOk();
              if (!isTransactionOk) {
                return Mono.error(
                    new PaymentExecutionException(
                        "Could not transfer invoice payment amount from buyer to seller."));
              }

              return Mono.empty();
            });
  }

  private BigDecimal getPaymentAmountFromMessage(
      InvoiceSettlementEventMessage invoiceSettlementMessage) {
    BigDecimal paymentAmountInCents = new BigDecimal(invoiceSettlementMessage.getPaymentAmount());
    return MonetaryAmountUtils.convertCentsToDollars(paymentAmountInCents);
  }
}
