package com.tcmp.tiapi.invoice.service;

import com.tcmp.tiapi.customer.model.Account;
import com.tcmp.tiapi.customer.model.Customer;
import com.tcmp.tiapi.customer.repository.AccountRepository;
import com.tcmp.tiapi.customer.repository.CustomerRepository;
import com.tcmp.tiapi.invoice.dto.ti.financeack.FinanceAckMessage;
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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceFinancingService {
  private final ProductMasterExtensionRepository productMasterExtensionRepository;
  private final ProgramExtensionRepository programExtensionRepository;
  private final CustomerRepository customerRepository;
  private final AccountRepository accountRepository;
  private final InvoiceRepository invoiceRepository;

  @Value("${bp.service.payment-execution.bgl-account}")
  private String bglAccount;

  public Customer findCustomerByMnemonic(String customerMnemonic) {
    return customerRepository
        .findFirstByIdMnemonic(customerMnemonic)
        .orElseThrow(
            () ->
                new EntityNotFoundException(
                    "Could not find customer with mnemonic " + customerMnemonic));
  }

  public ProductMasterExtension findProductMasterExtensionByMasterReference(
      String invoiceMasterReference) {
    return productMasterExtensionRepository
        .findByMasterReference(invoiceMasterReference)
        .orElseThrow(
            () ->
                new EntityNotFoundException(
                    "Could not find account for the given invoice master."));
  }

  public InvoiceMaster findInvoiceByMasterReference(String invoiceMasterReference) {
    return invoiceRepository
        .findByProductMasterMasterReference(invoiceMasterReference)
        .orElseThrow(
            () ->
                new EntityNotFoundException(
                    "Could not find invoice for the given invoice master."));
  }

  public Account findAccountByCustomerMnemonic(String customerMnemonic) {
    return accountRepository
        .findByTypeAndCustomerMnemonic("CA", customerMnemonic)
        .orElseThrow(
            () ->
                new EntityNotFoundException(
                    "Could not find account for seller " + customerMnemonic));
  }

  public ProgramExtension findByProgrammeIdOrDefault(String programmeId) {
    return programExtensionRepository
        .findByProgrammeId(programmeId)
        .orElse(
            ProgramExtension.builder()
                .programmeId(programmeId)
                .extraFinancingDays(0)
                .requiresExtraFinancing(false)
                .build());
  }

  public DistributorCreditRequest buildDistributorCreditRequest(
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

  public TransactionRequest buildBuyerToBglTransactionRequest(
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

  public TransactionRequest buildBglToSellerTransactionRequest(
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

  public TransactionRequest buildSellerToBglSolcaAndTaxesTransactionRequest(
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

  public TransactionRequest buildBglToBuyerSolcaAndTaxesTransactionRequest(
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

  public InvoiceEmailInfo buildInvoiceFinancingEmailInfo(
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

  private BigDecimal getFinanceDealAmountFromMessage(FinanceAckMessage invoiceFinanceAck) {
    BigDecimal financeDealAmountInCents = new BigDecimal(invoiceFinanceAck.getFinanceDealAmount());
    return MonetaryAmountUtils.convertCentsToDollars(financeDealAmountInCents);
  }
}
