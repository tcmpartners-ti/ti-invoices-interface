package com.tcmp.tiapi.invoice.service;

import com.tcmp.tiapi.customer.model.Account;
import com.tcmp.tiapi.customer.model.Address;
import com.tcmp.tiapi.customer.model.Customer;
import com.tcmp.tiapi.customer.model.CustomerId;
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
import com.tcmp.tiapi.titoapigee.operationalgateway.model.InvoiceEmailEvent;
import com.tcmp.tiapi.titoapigee.paymentexecution.dto.request.TransactionType;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvoiceSettlementServiceTest {
  @Mock private AccountRepository accountRepository;
  @Mock private CustomerRepository customerRepository;
  @Mock private InvoiceRepository invoiceRepository;
  @Mock private ProductMasterExtensionRepository productMasterExtensionRepository;
  @Mock private ProgramExtensionRepository programExtensionRepository;

  private InvoiceSettlementService invoiceSettlementService;

  @BeforeEach
  void setUp() {
    invoiceSettlementService = new InvoiceSettlementService(
      accountRepository,
      customerRepository,
      invoiceRepository,
      productMasterExtensionRepository,
      programExtensionRepository
    );
  }

  @Test
  void findInvoiceByMasterRef_itShouldThrowExceptionWhenNotFound() {
    when(invoiceRepository.findByProductMasterMasterReference(anyString()))
      .thenReturn(Optional.empty());

    assertThrows(
      EntityNotFoundException.class,
      () -> invoiceSettlementService.findInvoiceByMasterRef("")
    );
  }

  @Test
  void findInvoiceByMasterRef_itShouldReturnInvoice() {
    when(invoiceRepository.findByProductMasterMasterReference(anyString()))
      .thenReturn(Optional.of(InvoiceMaster.builder().build()));

    var actualInvoice = invoiceSettlementService.findInvoiceByMasterRef("");

    assertNotNull(actualInvoice);
  }

  @Test
  void findCustomerByMnemonic_itShouldThrowExceptionWhenNotFound() {
    when(customerRepository.findFirstByIdMnemonic(anyString()))
      .thenReturn(Optional.empty());

    assertThrows(
      EntityNotFoundException.class,
      () -> invoiceSettlementService.findCustomerByMnemonic("")
    );
  }

  @Test
  void findCustomerByMnemonic_itShouldReturnCustomer() {
    when(customerRepository.findFirstByIdMnemonic(anyString()))
      .thenReturn(Optional.of(Customer.builder().build()));

    var actualCustomer = invoiceSettlementService.findCustomerByMnemonic("");

    assertNotNull(actualCustomer);
  }


  @Test
  void findProductMasterExtension_itShouldThrowExceptionWhenNotFound() {
    when(productMasterExtensionRepository.findByMasterId(anyLong()))
      .thenReturn(Optional.empty());

    assertThrows(
      EntityNotFoundException.class,
      () -> invoiceSettlementService.findProductMasterExtensionByMasterId(1L)
    );
  }

  @Test
  void findProductMasterExtension_itShouldReturnExtension() {
    when(productMasterExtensionRepository.findByMasterId(anyLong()))
      .thenReturn(Optional.of(ProductMasterExtension.builder().build()));

    var actualProductMasterExtension = invoiceSettlementService.findProductMasterExtensionByMasterId(1L);

    assertNotNull(actualProductMasterExtension);
  }


  @Test
  void findAccountByCustomerMnemonic_itShouldThrowExceptionWhenNotFound() {
    when(accountRepository.findByTypeAndCustomerMnemonic(eq("CA"), anyString()))
      .thenReturn(Optional.empty());

    assertThrows(
      EntityNotFoundException.class,
      () -> invoiceSettlementService.findAccountByCustomerMnemonic("")
    );
  }

  @Test
  void findAccountByCustomerMnemonic_itShouldReturnAccount() {
    when(accountRepository.findByTypeAndCustomerMnemonic(eq("CA"), anyString()))
      .thenReturn(Optional.of(Account.builder().build()));

    var actualAccount = invoiceSettlementService.findAccountByCustomerMnemonic("");

    assertNotNull(actualAccount);
  }

  @Test
  void findByProgrammeIdOrDefault_itShouldReturnDefaultProgramExtensionWhenNotFound() {
    when(programExtensionRepository.findByProgrammeId(anyString()))
      .thenReturn(Optional.empty());

    var actualProgramExtension = invoiceSettlementService.findByProgrammeIdOrDefault("");

    assertEquals("", actualProgramExtension.getProgrammeId());
    assertEquals(0, actualProgramExtension.getExtraFinancingDays());
    assertEquals(false, actualProgramExtension.getRequiresExtraFinancing());
  }

  @Test
  void findByProgrammeIdOrDefault_itShouldReturnProgrammeExtension() {
    when(programExtensionRepository.findByProgrammeId(anyString()))
      .thenReturn(Optional.of(ProgramExtension.builder()
        .programmeId("Programme123")
        .requiresExtraFinancing(true)
        .extraFinancingDays(30)
        .build()));

    var actualProgramExtension = invoiceSettlementService.findByProgrammeIdOrDefault("Programme123");

    assertEquals("Programme123", actualProgramExtension.getProgrammeId());
    assertEquals(30, actualProgramExtension.getExtraFinancingDays());
    assertEquals(true, actualProgramExtension.getRequiresExtraFinancing());
  }

  private static Stream<Arguments> provideInvoiceHasNoLinkedFinancedEvent() {
    var firstInvoice = InvoiceMaster.builder()
      .isDrawDownEligible(false)
      .createFinanceEventId(null)
      .discountDealAmount(null)
      .build();

    var secondInvoice = InvoiceMaster.builder()
      .isDrawDownEligible(true)
      .createFinanceEventId(1L)
      .discountDealAmount(BigDecimal.ZERO)
      .build();

    return Stream.of(
      Arguments.of(firstInvoice, false),
      Arguments.of(secondInvoice, false)
    );
  }

  @ParameterizedTest
  @MethodSource("provideInvoiceHasNoLinkedFinancedEvent")
  void invoiceHasLinkedFinanceEvent_itShouldReturnFalseForInvoicesWithNoFinanceEvent(InvoiceMaster invoiceMaster) {
    assertFalse(invoiceSettlementService.invoiceHasLinkedFinanceEvent(invoiceMaster));
  }

  private static Stream<Arguments> provideInvoiceHasLinkedFinancedEvent() {
    var firstInvoice = InvoiceMaster.builder()
      .isDrawDownEligible(false)
      .createFinanceEventId(1L)
      .discountDealAmount(BigDecimal.TEN)
      .build();

    var secondInvoice = InvoiceMaster.builder()
      .isDrawDownEligible(false)
      .createFinanceEventId(1L)
      .discountDealAmount(BigDecimal.valueOf(200))
      .build();

    return Stream.of(
      Arguments.of(firstInvoice, true),
      Arguments.of(secondInvoice, true)
    );
  }

  @ParameterizedTest
  @MethodSource("provideInvoiceHasLinkedFinancedEvent")
  void invoiceHasLinkedFinanceEvent_itShouldReturnTrueForInvoicesWithFinanceEvent(InvoiceMaster invoiceMaster) {
    assertTrue(invoiceSettlementService.invoiceHasLinkedFinanceEvent(invoiceMaster));
  }

  @Test
  void buildDistributorCreditRequest_itShouldBuildRequest() {
    var invoiceSettlementMessage = CreateDueInvoiceEventMessage.builder()
      .paymentAmount("100")
      .receivedOn("2023-11-06")
      .paymentValueDate("2023-11-30")
      .build();

    var programExtension = ProgramExtension.builder()
      .programmeId("Programme1")
      .extraFinancingDays(30)
      .requiresExtraFinancing(true)
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

    var creditRequest = invoiceSettlementService.buildDistributorCreditRequest(
      invoiceSettlementMessage, buyer, programExtension, buyerAccountParser);

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
    assertEquals("001", creditRequest.interestPayment().gracePeriod().installmentNumber());
    assertEquals(30, creditRequest.term());
  }

  @Test
  void buildBglToSellerTransaction_itShouldBuildRequest() {
    var invoiceSettlementMessage = CreateDueInvoiceEventMessage.builder()
      .invoiceNumber("01-001")
      .paymentCurrency("USD")
      .paymentAmount("123")
      .build();

    var buyer = Customer.builder()
      .fullName("David Reyes     ")
      .build();

    var sellerAccountParser = new EncodedAccountParser("CC2777371930");

    var actualRequest = invoiceSettlementService.buildBglToSellerTransaction(
      invoiceSettlementMessage, buyer, sellerAccountParser);

    assertNotNull(actualRequest);
    assertEquals(TransactionType.BGL_TO_CLIENT.getValue(), actualRequest.transactionType());
    assertEquals("Pago Factura 01-001 David Reyes", actualRequest.transaction().concept());
  }

  @Test
  void buildInvoiceSettlementEmailInfo_itShouldBuildRequest() {
    var invoiceSettlementMessage = CreateDueInvoiceEventMessage.builder()
      .buyerIdentifier("1722466420001")
      .paymentValueDate("2023-01-01")
      .paymentCurrency("USD")
      .invoiceNumber("01-01")
      .build();

    var customer = Customer.builder()
      .address(Address.builder().customerEmail("david@mail.com  ").build())
      .fullName("David Reyes    ")
      .build();

    var actualRequest = invoiceSettlementService.buildInvoiceSettlementEmailInfo(
      invoiceSettlementMessage, customer, InvoiceEmailEvent.PROCESSED, BigDecimal.TEN);

    assertNotNull(actualRequest);
  }
}
