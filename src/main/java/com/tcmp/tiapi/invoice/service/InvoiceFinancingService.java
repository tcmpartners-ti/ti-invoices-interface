package com.tcmp.tiapi.invoice.service;

import com.tcmp.tiapi.customer.model.Account;
import com.tcmp.tiapi.customer.model.Customer;
import com.tcmp.tiapi.customer.repository.AccountRepository;
import com.tcmp.tiapi.customer.repository.CustomerRepository;
import com.tcmp.tiapi.invoice.dto.ti.financeack.FinanceAckMessage;
import com.tcmp.tiapi.invoice.model.ProductMasterExtension;
import com.tcmp.tiapi.invoice.repository.ProductMasterExtensionRepository;
import com.tcmp.tiapi.invoice.util.EncodedAccountParser;
import com.tcmp.tiapi.shared.utils.MonetaryAmountUtils;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.request.AmortizationPaymentPeriodType;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.request.CommercialTrade;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.request.Disbursement;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.request.DistributorCreditRequest;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.request.GracePeriod;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.request.InterestPayment;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.request.PaymentForm;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.request.Tax;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.request.TermPeriodType;
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
public class InvoiceFinancingService {
  private final ProductMasterExtensionRepository productMasterExtensionRepository;
  private final CustomerRepository customerRepository;
  private final AccountRepository accountRepository;

  @Value("${bp.service.payment-execution.bgl-account}") private String bglAccount;

  public Customer findCustomerByMnemonic(String customerMnemonic) {
    return customerRepository.findFirstByIdMnemonic(customerMnemonic)
      .orElseThrow(() -> new EntityNotFoundException("Could not find customer with mnemonic " + customerMnemonic));
  }

  public ProductMasterExtension findProductMasterExtensionByMasterReference(String invoiceMasterReference) {
    return productMasterExtensionRepository.findByMasterReference(invoiceMasterReference)
      .orElseThrow(() -> new EntityNotFoundException("Could not find account for the given invoice master."));
  }

  public Account findAccountByCustomerMnemonic(String customerMnemonic) {
    return accountRepository.findByTypeAndCustomerMnemonic("CA", customerMnemonic)
      .orElseThrow(() -> new EntityNotFoundException("Could not find account for seller " + customerMnemonic));
  }

  public DistributorCreditRequest buildDistributorCreditRequest(
    FinanceAckMessage invoiceFinanceAck,
    Customer buyer,
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
      .amount(getFinanceDealAmountFromMessage(invoiceFinanceAck))
      .effectiveDate(invoiceFinanceAck.getReceivedOn()) // Consultar con Nas
      .firstDueDate(invoiceFinanceAck.getMaturityDate())
      .term(0)
      .termPeriodType(new TermPeriodType("D"))
      .amortizationPaymentPeriodType(new AmortizationPaymentPeriodType("FIN"))
      .interestPayment(new InterestPayment("FIN", new GracePeriod("O", "002")))
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

  public TransactionRequest buildBuyerToBglTransactionRequest(
    DistributorCreditResponse distributorCreditResponse,
    FinanceAckMessage invoiceFinanceMessage,
    EncodedAccountParser buyerAccountParser
  ) {
    String invoiceReference = invoiceFinanceMessage.getTheirRef();
    String sellerName = invoiceFinanceMessage.getSellerName();
    String currency = invoiceFinanceMessage.getPaymentDetails().getCurrency();
    BigDecimal amount = BigDecimal.valueOf(distributorCreditResponse.data().disbursementAmount());

    return TransactionRequest.from(
      TransactionType.CLIENT_TO_BGL,
      buyerAccountParser.getAccount(),
      bglAccount,
      String.format("Descuento Factura %s %s", invoiceReference, sellerName),
      currency,
      amount
    );
  }

  public TransactionRequest buildBglToSellerTransactionRequest(
    DistributorCreditResponse distributorCreditResponse,
    FinanceAckMessage invoicePrepaymentMessage,
    EncodedAccountParser sellerAccount
  ) {
    String invoiceReference = invoicePrepaymentMessage.getTheirRef();
    String buyerName = invoicePrepaymentMessage.getBuyerName();
    String currency = invoicePrepaymentMessage.getPaymentDetails().getCurrency();
    BigDecimal amount = BigDecimal.valueOf(distributorCreditResponse.data().disbursementAmount());

    return TransactionRequest.from(
      TransactionType.BGL_TO_CLIENT,
      bglAccount,
      sellerAccount.getAccount(),
      String.format("Pago Factura %s %s", invoiceReference, buyerName),
      currency,
      amount
    );
  }

  public InvoiceEmailInfo buildInvoiceFinancingEmailInfo(
    FinanceAckMessage invoiceFinanceAck,
    Customer customer,
    InvoiceEmailEvent event
  ) {
    return InvoiceEmailInfo.builder()
      .customerMnemonic(invoiceFinanceAck.getSellerIdentifier())
      .customerEmail(customer.getAddress().getCustomerEmail().trim())
      .customerName(customer.getFullName().trim())
      .date(invoiceFinanceAck.getReceivedOn())
      .action(event.getValue())
      .invoiceCurrency(invoiceFinanceAck.getFinanceDealCurrency())
      .invoiceNumber(invoiceFinanceAck.getTheirRef())
      .amount(getFinanceDealAmountFromMessage(invoiceFinanceAck))
      .build();
  }

  private BigDecimal getFinanceDealAmountFromMessage(FinanceAckMessage invoiceFinanceAck) {
    BigDecimal financeDealAmountInCents = new BigDecimal(invoiceFinanceAck.getFinanceDealAmount());
    return MonetaryAmountUtils.convertCentsToDollars(financeDealAmountInCents);
  }
}
