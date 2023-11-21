package com.tcmp.tiapi.invoice.service;

import com.tcmp.tiapi.customer.model.Account;
import com.tcmp.tiapi.customer.model.Customer;
import com.tcmp.tiapi.customer.repository.AccountRepository;
import com.tcmp.tiapi.customer.repository.CustomerRepository;
import com.tcmp.tiapi.invoice.dto.ti.CreateDueInvoiceEventMessage;
import com.tcmp.tiapi.invoice.model.InvoiceMaster;
import com.tcmp.tiapi.invoice.model.ProductMasterExtension;
import com.tcmp.tiapi.invoice.repository.InvoiceRepository;
import com.tcmp.tiapi.invoice.repository.ProductMasterExtensionRepository;
import com.tcmp.tiapi.invoice.util.EncodedAccountParser;
import com.tcmp.tiapi.program.model.ProgramExtension;
import com.tcmp.tiapi.program.repository.ProgramExtensionRepository;
import com.tcmp.tiapi.shared.ApplicationEnv;
import com.tcmp.tiapi.shared.utils.MonetaryAmountUtils;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.request.*;
import com.tcmp.tiapi.titoapigee.operationalgateway.model.InvoiceEmailEvent;
import com.tcmp.tiapi.titoapigee.operationalgateway.model.InvoiceEmailInfo;
import com.tcmp.tiapi.titoapigee.paymentexecution.dto.request.TransactionRequest;
import com.tcmp.tiapi.titoapigee.paymentexecution.dto.request.TransactionType;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceSettlementService {
  private final AccountRepository accountRepository;
  private final CustomerRepository customerRepository;
  private final InvoiceRepository invoiceRepository;
  private final ProductMasterExtensionRepository productMasterExtensionRepository;
  private final ProgramExtensionRepository programExtensionRepository;

  @Value("${spring.profiles.active}") private String activeProfile;
  @Value("${bp.service.payment-execution.bgl-account}") private String bglAccount;

  public InvoiceMaster findInvoiceByMasterRef(String masterReference) {
    return invoiceRepository.findByProductMasterMasterReference(masterReference)
      .orElseThrow(() -> new EntityNotFoundException("Could not find invoice."));
  }

  public Customer findCustomerByMnemonic(String customerMnemonic) {
    return customerRepository.findFirstByIdMnemonic(customerMnemonic)
      .orElseThrow(() -> new EntityNotFoundException("Could not find customer with mnemonic " + customerMnemonic));
  }

  public ProductMasterExtension findProductMasterExtensionByMasterId(Long invoiceMasterId) {
    return productMasterExtensionRepository.findByMasterId(invoiceMasterId)
      .orElseThrow(() -> new EntityNotFoundException("Could not find account for the given invoice master."));
  }

  public Account findAccountByCustomerMnemonic(String customerMnemonic) {
    return accountRepository.findByTypeAndCustomerMnemonic("CA", customerMnemonic)
      .orElseThrow(() -> new EntityNotFoundException("Could not find account for seller " + customerMnemonic));
  }

  public ProgramExtension findProgrammeExtensionByIdOrDefault(String programmeId) {
    return programExtensionRepository.findByProgrammeId(programmeId)
      .orElse(ProgramExtension.builder()
        .programmeId(programmeId)
        .extraFinancingDays(0)
        .requiresExtraFinancing(false)
        .build());
  }

  public boolean invoiceHasLinkedFinanceEvent(InvoiceMaster invoice) {
    BigDecimal discountDealAmount = invoice.getDiscountDealAmount();

    return !invoice.getIsDrawDownEligible()
           && invoice.getCreateFinanceEventId() != null
           && (discountDealAmount != null && BigDecimal.ZERO.compareTo(discountDealAmount) != 0);
  }

  public DistributorCreditRequest buildDistributorCreditRequest(
    CreateDueInvoiceEventMessage invoiceSettlementMessage,
    Customer buyer,
    ProgramExtension programExtension,
    EncodedAccountParser buyerAccountParser
  ) {
    // Mock this value for dev testing purposes.
    boolean isDevelopment = ApplicationEnv.LOCAL.value().equals(activeProfile) || ApplicationEnv.DEV.value().equals(activeProfile);
    String paymentValueDate = isDevelopment
      ? getSystemDate()
      : invoiceSettlementMessage.getPaymentValueDate();

    return DistributorCreditRequest.builder()
      .commercialTrade(new CommercialTrade(buyer.getType().trim()))
      .customer(com.tcmp.tiapi.titoapigee.corporateloan.dto.request.Customer.builder()
        .customerId(buyer.getNumber().trim())
        .documentNumber(buyer.getId().getMnemonic().trim())
        .fullName(buyer.getFullName().trim())
        .documentType(buyer.getBankCode1().trim())
        .build())
      .disbursement(Disbursement.builder()
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
      .tax(Tax.builder()
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

  public TransactionRequest buildBuyerToBglTransactionRequest(
    CreateDueInvoiceEventMessage invoiceSettlementMessage,
    Customer seller,
    EncodedAccountParser buyerAccountParser
  ) {
    String invoiceReference = invoiceSettlementMessage.getInvoiceNumber();
    String sellerName = seller.getFullName().trim();
    String concept = String.format("Descuento Factura %s %s", invoiceReference, sellerName);
    String currency = invoiceSettlementMessage.getPaymentCurrency();
    BigDecimal amount = getPaymentAmountFromMessage(invoiceSettlementMessage);

    return TransactionRequest.from(
      TransactionType.CLIENT_TO_BGL, buyerAccountParser.getAccount(), bglAccount, concept, currency, amount);
  }

  public TransactionRequest buildBglToSellerTransaction(
    CreateDueInvoiceEventMessage invoiceSettlementMessage,
    Customer buyer,
    EncodedAccountParser sellerAccountParser
  ) {
    String invoiceReference = invoiceSettlementMessage.getInvoiceNumber();
    String buyerName = buyer.getFullName().trim();
    String concept = String.format("Pago Factura %s %s", invoiceReference, buyerName);
    String currency = invoiceSettlementMessage.getPaymentCurrency();
    BigDecimal amount = getPaymentAmountFromMessage(invoiceSettlementMessage);

    return TransactionRequest.from(
      TransactionType.BGL_TO_CLIENT, bglAccount, sellerAccountParser.getAccount(), concept, currency, amount);
  }

  private BigDecimal getPaymentAmountFromMessage(CreateDueInvoiceEventMessage invoiceSettlementMessage) {
    BigDecimal paymentAmountInCents = new BigDecimal(invoiceSettlementMessage.getPaymentAmount());
    return MonetaryAmountUtils.convertCentsToDollars(paymentAmountInCents);
  }

  public InvoiceEmailInfo buildInvoiceSettlementEmailInfo(
    InvoiceEmailEvent event,
    CreateDueInvoiceEventMessage message,
    Customer customer,
    BigDecimal amount
  ) {
    return InvoiceEmailInfo.builder()
      .customerMnemonic(message.getBuyerIdentifier())
      .customerEmail(customer.getAddress().getCustomerEmail().trim())
      .customerName(customer.getFullName().trim())
      .date(message.getPaymentValueDate())
      .action(event.getValue())
      .invoiceCurrency(message.getPaymentCurrency().trim())
      .invoiceNumber(message.getInvoiceNumber())
      .amount(amount)
      .build();
  }
}
