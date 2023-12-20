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
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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

  @Override
  public void handleServiceRequest(AckServiceRequest<?> serviceRequest) {
    InvoiceSettlementEventMessage message =
        (InvoiceSettlementEventMessage) serviceRequest.getBody();
    BigDecimal paymentAmountInCents = new BigDecimal(message.getPaymentAmount());
    BigDecimal paymentAmount = MonetaryAmountUtils.convertCentsToDollars(paymentAmountInCents);

    Customer buyer = findCustomerByMnemonic(message.getBuyerIdentifier());
    Customer seller = findCustomerByMnemonic(message.getSellerIdentifier());
    ProgramExtension programExtension = findProgrammeExtensionByIdOrDefault(message.getProgramme());
    InvoiceMaster invoice = findInvoiceByMasterRef(message.getMasterRef());
    ProductMasterExtension invoiceExtension = findProductMasterExtensionByMasterReference(message.getMasterRef());

    EncodedAccountParser buyerAccountParser =
        new EncodedAccountParser(invoiceExtension.getFinanceAccount());

    boolean hasExtraFinancingDays = programExtension.getExtraFinancingDays() > 0;
    boolean hasBeenFinanced = invoiceHasLinkedFinanceEvent(invoice);

    // For Mvp, we ignore invoices with no extra financing days.
    if (!hasExtraFinancingDays) {
      log.info("Programe has no extra financing days, flow ended.");
      return;
    }

    try {
      // Second settlement case: end flow, we only notify credit to buyer.
      if (hasBeenFinanced) {
        sendInvoiceStatusEmailToCustomer(InvoiceEmailEvent.CREDITED, message, buyer, paymentAmount);
        return;
      }

      // Not financed invoices flow
      sendInvoiceStatusEmailToCustomer(InvoiceEmailEvent.SETTLED, message, seller, paymentAmount);
      createBuyerCreditOrThrowException(message, buyer, programExtension, buyerAccountParser);
      sendInvoiceStatusEmailToCustomer(InvoiceEmailEvent.CREDITED, message, buyer, paymentAmount);
      transferPaymentAmountFromBuyerToSellerOrThrowException(
          message, buyer, seller, buyerAccountParser);
      sendInvoiceStatusEmailToCustomer(InvoiceEmailEvent.PROCESSED, message, seller, paymentAmount);

      notifySettlementStatusExternally(PayloadStatus.SUCCEEDED, message, invoice, null);
    } catch (CreditCreationException | PaymentExecutionException e) {
      log.error(e.getMessage());
      notifySettlementStatusExternally(PayloadStatus.FAILED, message, invoice, e.getMessage());
    }
  }

  private InvoiceMaster findInvoiceByMasterRef(String masterReference) {
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

  private ProductMasterExtension findProductMasterExtensionByMasterReference(String masterReference) {
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

  private void createBuyerCreditOrThrowException(
      InvoiceSettlementEventMessage message,
      Customer buyer,
      ProgramExtension programExtension,
      EncodedAccountParser buyerAccountParser)
      throws CreditCreationException {
    log.info("Started credit creation.");
    DistributorCreditRequest creditRequest =
        buildDistributorCreditRequest(message, buyer, programExtension, buyerAccountParser);
    DistributorCreditResponse creditResponse = corporateLoanService.createCredit(creditRequest);
    Error creditError = creditResponse.data().error();

    boolean hasBeenCredited = creditError != null && creditError.hasNoError();
    if (!hasBeenCredited) {
      String creditErrorMessage =
          creditError != null ? creditError.message() : "Credit creation failed.";
      throw new CreditCreationException(creditErrorMessage);
    }
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

  private void transferPaymentAmountFromBuyerToSellerOrThrowException(
      InvoiceSettlementEventMessage message,
      Customer buyer,
      Customer seller,
      EncodedAccountParser buyerAccountParser)
      throws PaymentExecutionException {
    log.info("Started buyer to seller transaction.");
    Account sellerAccount = findAccountByCustomerMnemonic(message.getSellerIdentifier());
    EncodedAccountParser sellerAccountParser =
        new EncodedAccountParser(sellerAccount.getExternalAccountNumber());

    BusinessAccountTransfersResponse buyerToBglTransactionResponse =
        paymentExecutionService.makeTransactionRequest(
            buildBuyerToBglTransactionRequest(message, seller, buyerAccountParser));

    BusinessAccountTransfersResponse bglToSellerTransactionResponse =
        paymentExecutionService.makeTransactionRequest(
            buildBglToSellerTransaction(message, buyer, sellerAccountParser));

    boolean isBuyerToSellerTransactionOk =
        buyerToBglTransactionResponse != null
            && bglToSellerTransactionResponse != null
            && buyerToBglTransactionResponse.isOk()
            && bglToSellerTransactionResponse.isOk();
    if (!isBuyerToSellerTransactionOk) {
      throw new PaymentExecutionException(
          "Could not transfer invoice payment amount from buyer to seller.");
    }
  }

  private TransactionRequest buildBuyerToBglTransactionRequest(
      InvoiceSettlementEventMessage invoiceSettlementMessage,
      Customer seller,
      EncodedAccountParser buyerAccountParser) {
    String invoiceReference = invoiceSettlementMessage.getInvoiceNumber();
    String sellerName = seller.getFullName().trim();
    String concept = String.format("Descuento Factura %s %s", invoiceReference, sellerName);
    String currency = invoiceSettlementMessage.getPaymentCurrency();
    BigDecimal amount = getPaymentAmountFromMessage(invoiceSettlementMessage);

    return TransactionRequest.from(
        TransactionType.CLIENT_TO_BGL,
        buyerAccountParser.getAccount(),
        bglAccount,
        concept,
        currency,
        amount);
  }

  private TransactionRequest buildBglToSellerTransaction(
      InvoiceSettlementEventMessage invoiceSettlementMessage,
      Customer buyer,
      EncodedAccountParser sellerAccountParser) {
    String invoiceReference = invoiceSettlementMessage.getInvoiceNumber();
    String buyerName = buyer.getFullName().trim();
    String concept = String.format("Pago Factura %s %s", invoiceReference, buyerName);
    String currency = invoiceSettlementMessage.getPaymentCurrency();
    BigDecimal amount = getPaymentAmountFromMessage(invoiceSettlementMessage);

    return TransactionRequest.from(
        TransactionType.BGL_TO_CLIENT,
        bglAccount,
        sellerAccountParser.getAccount(),
        concept,
        currency,
        amount);
  }

  private BigDecimal getPaymentAmountFromMessage(
      InvoiceSettlementEventMessage invoiceSettlementMessage) {
    BigDecimal paymentAmountInCents = new BigDecimal(invoiceSettlementMessage.getPaymentAmount());
    return MonetaryAmountUtils.convertCentsToDollars(paymentAmountInCents);
  }

  private void sendInvoiceStatusEmailToCustomer(
      InvoiceEmailEvent event,
      InvoiceSettlementEventMessage message,
      Customer customer,
      BigDecimal paymentAmount) {
    try {
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

      operationalGatewayService.sendNotificationRequest(creditedInvoiceInfo);
    } catch (OperationalGatewayException e) {
      log.error(e.getMessage());
    }
  }

  private void notifySettlementStatusExternally(
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

    businessBankingService.notifyEvent(OperationalGatewayProcessCode.INVOICE_SETTLEMENT, payload);
  }
}
