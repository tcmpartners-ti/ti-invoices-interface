package com.tcmp.tiapi.invoice.service;

import com.tcmp.tiapi.customer.model.Account;
import com.tcmp.tiapi.customer.model.Address;
import com.tcmp.tiapi.customer.model.Customer;
import com.tcmp.tiapi.customer.model.CustomerId;
import com.tcmp.tiapi.customer.repository.AccountRepository;
import com.tcmp.tiapi.customer.repository.CustomerRepository;
import com.tcmp.tiapi.invoice.dto.ti.financeack.FinanceAckMessage;
import com.tcmp.tiapi.invoice.dto.ti.financeack.FinancePaymentDetails;
import com.tcmp.tiapi.invoice.model.ProductMasterExtension;
import com.tcmp.tiapi.invoice.repository.ProductMasterExtensionRepository;
import com.tcmp.tiapi.invoice.util.EncodedAccountParser;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.response.Data;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.response.DistributorCreditResponse;
import com.tcmp.tiapi.titoapigee.operationalgateway.model.InvoiceEmailEvent;
import com.tcmp.tiapi.titoapigee.paymentexecution.dto.request.TransactionType;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvoiceFinancingServiceTest {
  @Mock private ProductMasterExtensionRepository productMasterExtensionRepository;
  @Mock private CustomerRepository customerRepository;
  @Mock private AccountRepository accountRepository;

  private InvoiceFinancingService invoiceFinancingService;

  @BeforeEach
  void setUp() {
    invoiceFinancingService = new InvoiceFinancingService(
      productMasterExtensionRepository,
      customerRepository,
      accountRepository
    );
  }

  @Test
  void findCustomerByMnemonic_itShouldThrowExceptionWhenNotFound() {
    when(customerRepository.findFirstByIdMnemonic(anyString()))
      .thenReturn(Optional.empty());

    assertThrows(
      EntityNotFoundException.class,
      () -> invoiceFinancingService.findCustomerByMnemonic("")
    );
  }

  @Test
  void findCustomerByMnemonic_itShouldReturnCustomer() {
    when(customerRepository.findFirstByIdMnemonic(anyString()))
      .thenReturn(Optional.of(Customer.builder().build()));

    var actualCustomer = invoiceFinancingService.findCustomerByMnemonic("");

    assertNotNull(actualCustomer);
  }

  @Test
  void findProductMasterExtension_itShouldThrowExceptionWhenNotFound() {
    when(productMasterExtensionRepository.findByMasterReference(anyString()))
      .thenReturn(Optional.empty());

    assertThrows(
      EntityNotFoundException.class,
      () -> invoiceFinancingService.findProductMasterExtensionByMasterReference("")
    );
  }

  @Test
  void findProductMasterExtension_itShouldReturnExtension() {
    when(productMasterExtensionRepository.findByMasterReference(anyString()))
      .thenReturn(Optional.of(ProductMasterExtension.builder().build()));

    var actualProductMasterExtension = invoiceFinancingService.findProductMasterExtensionByMasterReference("");

    assertNotNull(actualProductMasterExtension);
  }

  @Test
  void findAccountByCustomerMnemonic_itShouldThrowExceptionWhenNotFound() {
    when(accountRepository.findByTypeAndCustomerMnemonic(eq("CA"), anyString()))
      .thenReturn(Optional.empty());

    assertThrows(
      EntityNotFoundException.class,
      () -> invoiceFinancingService.findAccountByCustomerMnemonic("")
    );
  }

  @Test
  void findAccountByCustomerMnemonic_itShouldReturnAccount() {
    when(accountRepository.findByTypeAndCustomerMnemonic(eq("CA"), anyString()))
      .thenReturn(Optional.of(Account.builder().build()));

    var actualAccount = invoiceFinancingService.findAccountByCustomerMnemonic("");

    assertNotNull(actualAccount);
  }

  @Test
  void buildDistributorCreditRequest_itShouldBuildRequest() {
    var invoiceFinanceMessage = FinanceAckMessage.builder()
      .financeDealAmount("100")
      .receivedOn("2023-11-06")
      .maturityDate("2023-11-30")
      .build();

    var buyer = Customer.builder()
      .id(CustomerId.builder()
        .sourceBankingBusinessCode("BPEC")
        .mnemonic("1722466420002                    ") // TI saves this with extra spaces
        .build())
      .type("ADL")
      .number("1244188")
      .fullName("David                              ")
      .bankCode1("0003                              ")
      .build();

    var buyerAccountParser = new EncodedAccountParser("CC2777371930");

    var creditRequest = invoiceFinancingService.buildDistributorCreditRequest(
      invoiceFinanceMessage, buyer, buyerAccountParser);

    var expectedMnemonic = "1722466420002";
    var expectedName = "David";
    var expectedBankCode1 = "0003";

    assertNotNull(creditRequest);
    assertNotNull(creditRequest.customer());
    assertEquals(expectedMnemonic, creditRequest.customer().documentNumber());
    assertEquals(expectedName, creditRequest.customer().fullName());
    assertEquals(expectedBankCode1, creditRequest.customer().documentType());
    assertNotNull(creditRequest.disbursement());
    assertNotNull(creditRequest.effectiveDate());
    assertNotNull(creditRequest.firstDueDate());
    assertEquals("002", creditRequest.interestPayment().gracePeriod().installmentNumber());
  }

  @Test
  void buildBuyerToBglTransactionRequest_itShouldBuildRequest() {
    var creditorResponse = new DistributorCreditResponse(
      Data.builder()
        .disbursementAmount(200)
        .build()
    );

    var invoiceFinanceMessage = FinanceAckMessage.builder()
      .sellerName("David Reyes")
      .theirRef("001-001-000001")
      .paymentDetails(FinancePaymentDetails.builder()
        .currency("USD")
        .build())
      .build();

    var buyerAccountParser = new EncodedAccountParser("AH0129487194");

    var actualTransactionRequest = invoiceFinancingService.buildBuyerToBglTransactionRequest(
      creditorResponse, invoiceFinanceMessage, buyerAccountParser);

    var expectedAccount = "0129487194";
    assertNotNull(actualTransactionRequest);
    assertEquals(actualTransactionRequest.transactionType(), TransactionType.CLIENT_TO_BGL.getValue());
    assertEquals(expectedAccount, actualTransactionRequest.debtor().account().accountId());
    assertNotNull(actualTransactionRequest.transaction());
  }

  @Test
  void buildBglToSellerTransactionRequest_itShouldBuildRequest() {
    var creditorResponse = new DistributorCreditResponse(
      Data.builder()
        .disbursementAmount(200)
        .build()
    );

    var invoiceFinanceMessage = FinanceAckMessage.builder()
      .buyerName("SUPER MAXI")
      .theirRef("001-001-000001")
      .paymentDetails(FinancePaymentDetails.builder()
        .currency("USD")
        .build())
      .build();

    var sellerAccountParser = new EncodedAccountParser("AH0129487195");

    var actualTransactionRequest = invoiceFinancingService.buildBglToSellerTransactionRequest(
      creditorResponse, invoiceFinanceMessage, sellerAccountParser);

    var expectedAccount = "0129487195";
    assertNotNull(actualTransactionRequest);
    assertEquals(actualTransactionRequest.transactionType(), TransactionType.BGL_TO_CLIENT.getValue());
    assertEquals(expectedAccount, actualTransactionRequest.creditor().account().accountId());
    assertNotNull(actualTransactionRequest.transaction());
  }

  @Test
  void buildInvoiceFinancingEmailInfo_itShouldBuildRequest() {
    var financeAck = FinanceAckMessage.builder()
      .sellerIdentifier("1722466420001")
      .financeDealCurrency("USD")
      .theirRef("001-001-0001")
      .financeDealAmount("100")
      .build();

    var customer = Customer.builder()
      .fullName("David Reyes         ")
      .address(Address.builder()
        .customerEmail("dareyesp@pichincha.com")
        .build())
      .build();

    var actualRequest = invoiceFinancingService.buildInvoiceFinancingEmailInfo(
      financeAck, customer, InvoiceEmailEvent.PROCESSED);

    String expectedName = "David Reyes";
    assertNotNull(actualRequest);
    assertEquals(customer.getAddress().getCustomerEmail(), actualRequest.customerEmail());
    assertEquals(expectedName, actualRequest.customerName());
    assertNotNull(actualRequest.amount());
  }
}
