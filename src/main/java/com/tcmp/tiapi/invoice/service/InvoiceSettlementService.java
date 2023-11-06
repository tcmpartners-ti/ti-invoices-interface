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
import com.tcmp.tiapi.shared.utils.MonetaryAmountUtils;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.request.*;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.response.DistributorCreditResponse;
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

  @Value("${bp.service.payment-execution.bgl-account}") private String bglAccount;

  public InvoiceMaster findInvoiceByMasterRef(String masterReference) {
    return invoiceRepository.findByProductMasterMasterReference(masterReference)
      .orElseThrow(() -> new EntityNotFoundException("Could not find invoice."));
  }

  public Customer findCustomerByMnemonic(String customerMnemonic) {
    return customerRepository.findFirstByIdMnemonic(customerMnemonic)
      .orElseThrow(() -> new EntityNotFoundException("Could not find customer with mnemonic " + customerMnemonic));
  }

  public ProductMasterExtension findInvoiceExtension(InvoiceMaster invoiceMaster) {
    return productMasterExtensionRepository.findByMasterId(invoiceMaster.getId())
      .orElseThrow(() -> new EntityNotFoundException("Could not find account for the given invoice master."));
  }

  public Account findCustomerAccountByMnemonic(String customerMnemonic) {
    return accountRepository.findByTypeAndCustomerMnemonic("CA", customerMnemonic)
      .orElseThrow(() -> new EntityNotFoundException("Could not find account for seller " + customerMnemonic));
  }

  public ProgramExtension findByProgrammeIdOrDefault(String programmeId) {
    return programExtensionRepository.findByProgrammeId(programmeId)
      .orElse(ProgramExtension.builder()
        .programmeId(programmeId)
        .extraFinancingDays(0)
        .requiresExtraFinancing(false)
        .build());
  }

  public boolean invoiceHasLinkedPrepaymentEvent(InvoiceMaster invoice) {
    return !invoice.getIsDrawDownEligible()
           && invoice.getCreateFinanceEventId() != null
           && invoice.getDiscountDealAmount() != null
           && invoice.getDiscountDealAmount().compareTo(BigDecimal.ZERO) == 0;
  }

  public DistributorCreditRequest buildSettlementDistributorCreditRequest(
    CreateDueInvoiceEventMessage invoiceSettlementMessage,
    Customer buyer,
    ProgramExtension programExtension,
    EncodedAccountParser buyerAccountParser
  ) {
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
      .amount(getSettlementAmountFromInvoiceSettlementMessage(invoiceSettlementMessage))
      .effectiveDate(invoiceSettlementMessage.getPaymentValueDate())
      .firstDueDate(invoiceSettlementMessage.getPaymentValueDate())
      .term(programExtension.getExtraFinancingDays())
      .termPeriodType(new TermPeriodType("D"))
      .amortizationPaymentPeriodType(new AmortizationPaymentPeriodType("FIN"))
      .interestPayment(new InterestPayment("FIN", new GracePeriod("O", "001")))
      .maturityForm("FIJ")
      .quotaMaturityCriteria("*NO")
      .references(List.of())
      .tax(Tax.builder()
        .code("L")
        .paymentForm(new PaymentForm("D"))
        .rate(BigDecimal.ZERO)
        .amount(BigDecimal.ZERO)
        .build())
      .build();
  }

  public TransactionRequest buildBglToSellerTransaction(
    CreateDueInvoiceEventMessage invoiceSettlementMessage,
    DistributorCreditResponse settlementDistributorCreditResponse,
    Customer seller,
    EncodedAccountParser sellerAccountParser
  ) {
    String invoiceReference = invoiceSettlementMessage.getInvoiceNumber();
    String sellerName = seller.getFullName().trim();
    String concept = String.format("Pago factura %s %s", invoiceReference, sellerName);
    String currency = invoiceSettlementMessage.getPaymentCurrency();
    BigDecimal amount = BigDecimal.valueOf(settlementDistributorCreditResponse.data().disbursementAmount());

    return TransactionRequest.from(
      TransactionType.BGL_TO_CLIENT, bglAccount, sellerAccountParser.getAccount(), concept, currency, amount);
  }

  private BigDecimal getSettlementAmountFromInvoiceSettlementMessage(CreateDueInvoiceEventMessage invoiceSettlementMessage) {
    BigDecimal financeDealAmountInCents = new BigDecimal(invoiceSettlementMessage.getPaymentAmount());
    return MonetaryAmountUtils.convertCentsToDollars(financeDealAmountInCents);
  }

  public InvoiceEmailInfo buildInvoiceSettlementEmailInfo(
    CreateDueInvoiceEventMessage message,
    Customer customer,
    InvoiceEmailEvent event,
    BigDecimal amount
  ) {
    return InvoiceEmailInfo.builder()
      .customerMnemonic(message.getBuyerIdentifier())
      .customerEmail(customer.getAddress().getCustomerEmail().trim())
      .customerName(customer.getFullName().trim())
      .date(message.getReceivedOn())
      .action(event.getValue())
      .invoiceCurrency(message.getPaymentCurrency().trim())
      .invoiceNumber(message.getInvoiceNumber())
      .amount(amount)
      .build();
  }
}
