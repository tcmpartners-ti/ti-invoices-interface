package com.tcmp.tiapi.invoice.strategy.ticc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcmp.tiapi.customer.model.Account;
import com.tcmp.tiapi.customer.model.Address;
import com.tcmp.tiapi.customer.model.Customer;
import com.tcmp.tiapi.customer.model.CustomerId;
import com.tcmp.tiapi.customer.repository.AccountRepository;
import com.tcmp.tiapi.customer.repository.CustomerRepository;
import com.tcmp.tiapi.invoice.dto.ti.settle.InvoiceSettlementEventMessage;
import com.tcmp.tiapi.invoice.model.InvoiceMaster;
import com.tcmp.tiapi.invoice.model.ProductMasterExtension;
import com.tcmp.tiapi.invoice.repository.InvoiceRepository;
import com.tcmp.tiapi.invoice.repository.ProductMasterExtensionRepository;
import com.tcmp.tiapi.program.model.ProgramExtension;
import com.tcmp.tiapi.program.repository.ProgramExtensionRepository;
import com.tcmp.tiapi.shared.UUIDGenerator;
import com.tcmp.tiapi.ti.dto.request.AckServiceRequest;
import com.tcmp.tiapi.titoapigee.businessbanking.BusinessBankingService;
import com.tcmp.tiapi.titoapigee.businessbanking.dto.request.OperationalGatewayRequestPayload;
import com.tcmp.tiapi.titoapigee.businessbanking.dto.request.PayloadStatus;
import com.tcmp.tiapi.titoapigee.businessbanking.model.OperationalGatewayProcessCode;
import com.tcmp.tiapi.titoapigee.corporateloan.CorporateLoanService;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.CorporateLoanMapper;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.request.DistributorCreditRequest;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.response.Data;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.response.DistributorCreditResponse;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.response.Error;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.response.Tax;
import com.tcmp.tiapi.titoapigee.operationalgateway.OperationalGatewayService;
import com.tcmp.tiapi.titoapigee.operationalgateway.model.InvoiceEmailEvent;
import com.tcmp.tiapi.titoapigee.operationalgateway.model.InvoiceEmailInfo;
import com.tcmp.tiapi.titofcm.dto.SinglePaymentMapper;
import com.tcmp.tiapi.titofcm.dto.request.SinglePaymentRequest;
import com.tcmp.tiapi.titofcm.dto.response.SinglePaymentResponse;
import com.tcmp.tiapi.titofcm.exception.SinglePaymentException;
import com.tcmp.tiapi.titofcm.repository.InvoicePaymentCorrelationInfoRepository;
import com.tcmp.tiapi.titofcm.service.SingleElectronicPaymentService;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class InvoiceSettlementFlowStrategyTest {
  private static final String MOCK_BGL_ACCOUNT = "2374910290";
  private static final String DEBTOR_ID = "1182150";

  @Mock private UUIDGenerator uuidGenerator;
  @Mock private ObjectMapper objectMapper;

  @Mock private CorporateLoanService corporateLoanService;
  @Mock private OperationalGatewayService operationalGatewayService;
  @Mock private BusinessBankingService businessBankingService;
  @Mock private SingleElectronicPaymentService singleElectronicPaymentService;

  @Mock private AccountRepository accountRepository;
  @Mock private CustomerRepository customerRepository;
  @Mock private InvoicePaymentCorrelationInfoRepository invoicePaymentCorrelationInfoRepository;
  @Mock private InvoiceRepository invoiceRepository;
  @Mock private ProductMasterExtensionRepository productMasterExtensionRepository;
  @Mock private ProgramExtensionRepository programExtensionRepository;

  @Captor private ArgumentCaptor<InvoiceEmailInfo> invoiceEmailInfoArgumentCaptor;
  @Captor private ArgumentCaptor<OperationalGatewayRequestPayload> payloadArgumentCaptor;
  @Captor private ArgumentCaptor<SinglePaymentRequest> singlePaymentRequestArgumentCaptor;
  @Captor private ArgumentCaptor<DistributorCreditRequest> creditRequestArgumentCaptor;

  @InjectMocks private InvoiceSettlementFlowStrategy invoiceSettlementFlowStrategy;

  private Customer buyer;
  private Customer seller;

  @BeforeEach
  void setUp() {
    var mockedToday = LocalDate.of(2024, 2, 8);
    var mockedClock =
        Clock.fixed(
            mockedToday.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
    var singlePaymentMapper = Mappers.getMapper(SinglePaymentMapper.class);
    var corporateLoanMapper = Mappers.getMapper(CorporateLoanMapper.class);
    ReflectionTestUtils.setField(singlePaymentMapper, "bglAccount", MOCK_BGL_ACCOUNT);
    ReflectionTestUtils.setField(singlePaymentMapper, "debtorId", DEBTOR_ID);
    ReflectionTestUtils.setField(singlePaymentMapper, "clock", mockedClock);

    invoiceSettlementFlowStrategy =
        new InvoiceSettlementFlowStrategy(
            uuidGenerator,
            objectMapper,
            corporateLoanService,
            operationalGatewayService,
            businessBankingService,
            singleElectronicPaymentService,
            accountRepository,
            customerRepository,
            invoicePaymentCorrelationInfoRepository,
            invoiceRepository,
            productMasterExtensionRepository,
            programExtensionRepository,
            singlePaymentMapper,
            corporateLoanMapper);

    ReflectionTestUtils.setField(invoiceSettlementFlowStrategy, "activeProfile", "prod");
    // Mock injected fields
    buyer =
        Customer.builder()
            .id(CustomerId.builder().mnemonic("1722466420   ").build())
            .bankCode1("ABC   ")
            .type("B")
            .number("123")
            .fullName("Buyer     ")
            .address(Address.builder().customerEmail("buyer@mail.com").build())
            .build();
    seller =
        Customer.builder()
            .fullName("Seller")
            .address(Address.builder().customerEmail("seller@mail.com").build())
            .build();

    when(customerRepository.findFirstByIdMnemonic(anyString()))
        .thenReturn(Optional.of(buyer))
        .thenReturn(Optional.of(seller));
  }

  @Test
  void handleServiceRequest_itShouldSendEmailToBuyerIfExtraFinancedAndHasBeenFinanced() {
    InvoiceMaster financedInvoice =
        InvoiceMaster.builder()
            .id(1L)
            .batchId("123")
            .isDrawDownEligible(false)
            .createFinanceEventId(1L)
            .discountDealAmount(BigDecimal.TEN)
            .build();

    when(programExtensionRepository.findByProgrammeId(anyString()))
        .thenReturn(Optional.of(ProgramExtension.builder().extraFinancingDays(30).build()));
    when(invoiceRepository.findByProductMasterMasterReference(anyString()))
        .thenReturn(Optional.of(financedInvoice));
    when(productMasterExtensionRepository.findByMasterReference(anyString()))
        .thenReturn(
            Optional.of(ProductMasterExtension.builder().financeAccount("AH9278281280").build()));
    when(accountRepository.findByTypeAndCustomerMnemonic(anyString(), anyString()))
        .thenReturn(Optional.of(Account.builder().externalAccountNumber("AH9278281281").build()));

    invoiceSettlementFlowStrategy.handleServiceRequest(
        new AckServiceRequest<>(null, buildMockSettlementMessage()));

    verify(operationalGatewayService)
        .sendNotificationRequest(invoiceEmailInfoArgumentCaptor.capture());
    assertEquals(
        buyer.getAddress().getCustomerEmail(),
        invoiceEmailInfoArgumentCaptor.getValue().customerEmail());
    assertEquals(
        InvoiceEmailEvent.CREDITED.getValue(), invoiceEmailInfoArgumentCaptor.getValue().action());
  }

  @Test
  void handleServiceRequest_itShouldNotifyIfCreditCreationFails() {
    InvoiceMaster notFinancedInvoice =
        InvoiceMaster.builder()
            .id(1L)
            .batchId("123")
            .isDrawDownEligible(true)
            .createFinanceEventId(1L)
            .discountDealAmount(BigDecimal.TEN)
            .build();

    when(programExtensionRepository.findByProgrammeId(anyString()))
        .thenReturn(Optional.of(ProgramExtension.builder().extraFinancingDays(30).build()));
    when(invoiceRepository.findByProductMasterMasterReference(anyString()))
        .thenReturn(Optional.of(notFinancedInvoice));
    when(productMasterExtensionRepository.findByMasterReference(anyString()))
        .thenReturn(
            Optional.of(ProductMasterExtension.builder().financeAccount("AH9278281280").build()));
    when(accountRepository.findByTypeAndCustomerMnemonic(anyString(), anyString()))
        .thenReturn(Optional.of(Account.builder().externalAccountNumber("AH7381827031").build()));
    when(corporateLoanService.createCredit(any(DistributorCreditRequest.class)))
        .thenReturn(
            new DistributorCreditResponse(
                Data.builder().error(new Error("123", "Invalid credit.", "ERROR")).build()));

    invoiceSettlementFlowStrategy.handleServiceRequest(
        new AckServiceRequest<>(null, buildMockSettlementMessage()));

    verify(corporateLoanService).createCredit(any(DistributorCreditRequest.class));
    verify(businessBankingService)
        .notifyEvent(
            eq(OperationalGatewayProcessCode.INVOICE_SETTLEMENT), payloadArgumentCaptor.capture());
  }

  @Test
  void handleServiceRequest_itShouldNotifyIfTransactionFails() {
    InvoiceMaster notFinancedInvoice =
        InvoiceMaster.builder()
            .id(1L)
            .batchId("123")
            .isDrawDownEligible(true)
            .createFinanceEventId(1L)
            .discountDealAmount(BigDecimal.TEN)
            .build();

    when(programExtensionRepository.findByProgrammeId(anyString()))
        .thenReturn(Optional.of(ProgramExtension.builder().extraFinancingDays(30).build()));
    when(invoiceRepository.findByProductMasterMasterReference(anyString()))
        .thenReturn(Optional.of(notFinancedInvoice));
    when(productMasterExtensionRepository.findByMasterReference(anyString()))
        .thenReturn(
            Optional.of(ProductMasterExtension.builder().financeAccount("AH9278281280").build()));
    when(accountRepository.findByTypeAndCustomerMnemonic(anyString(), anyString()))
        .thenReturn(Optional.of(Account.builder().externalAccountNumber("AH9278281281").build()));
    when(corporateLoanService.createCredit(any()))
        .thenReturn(
            new DistributorCreditResponse(
                Data.builder()
                    .tax(Tax.builder().factor(8).amount(20).build())
                    .amortizations(List.of())
                    .error(new Error("", "", "INFO"))
                    .build()));
    when(singleElectronicPaymentService.createSinglePayment(any()))
        .thenThrow(new SinglePaymentException("Payment failed"));

    invoiceSettlementFlowStrategy.handleServiceRequest(
        new AckServiceRequest<>(null, buildMockSettlementMessage()));

    verify(corporateLoanService).createCredit(any());
    verify(businessBankingService)
        .notifyEvent(
            eq(OperationalGatewayProcessCode.INVOICE_SETTLEMENT), payloadArgumentCaptor.capture());

    var payload = payloadArgumentCaptor.getValue();
    assertEquals(PayloadStatus.FAILED.getValue(), payload.status());
  }

  @Test
  void handleServiceRequest_itShouldHandleHappyPath() {
    var notFinancedInvoice =
        InvoiceMaster.builder()
            .id(1L)
            .batchId("123")
            .isDrawDownEligible(true)
            .createFinanceEventId(1L)
            .discountDealAmount(BigDecimal.TEN)
            .build();

    when(uuidGenerator.getNewId()).thenReturn("001-001-001");
    when(programExtensionRepository.findByProgrammeId(anyString()))
        .thenReturn(Optional.of(ProgramExtension.builder().extraFinancingDays(30).build()));
    when(invoiceRepository.findByProductMasterMasterReference(anyString()))
        .thenReturn(Optional.of(notFinancedInvoice));
    when(productMasterExtensionRepository.findByMasterReference(anyString()))
        .thenReturn(
            Optional.of(ProductMasterExtension.builder().financeAccount("AH9278281280").build()));
    when(accountRepository.findByTypeAndCustomerMnemonic(anyString(), anyString()))
        .thenReturn(Optional.of(Account.builder().externalAccountNumber("AH9278281281").build()));
    when(corporateLoanService.createCredit(any()))
        .thenReturn(
            new DistributorCreditResponse(
                Data.builder()
                    .tax(Tax.builder().factor(8).amount(20).build())
                    .amortizations(List.of())
                    .error(new Error("", "", "INFO"))
                    .build()));
    when(singleElectronicPaymentService.createSinglePayment(any()))
        .thenReturn(new SinglePaymentResponse(new SinglePaymentResponse.Data("Reference123")));

    invoiceSettlementFlowStrategy.handleServiceRequest(
        new AckServiceRequest<>(null, buildMockSettlementMessage()));

    verify(corporateLoanService).createCredit(creditRequestArgumentCaptor.capture());
    verify(singleElectronicPaymentService)
        .createSinglePayment(singlePaymentRequestArgumentCaptor.capture());

    verify(operationalGatewayService, times(2))
        .sendNotificationRequest(invoiceEmailInfoArgumentCaptor.capture());

    assertCreditValues();
    assertTransactionsValues();
    assertEmailsValues();
  }

  private void assertCreditValues() {
    var creditRequest = creditRequestArgumentCaptor.getValue();
    assertNotNull(creditRequest);
    assertEquals("123", creditRequest.getCustomer().getCustomerId());
    assertEquals("1722466420", creditRequest.getCustomer().getDocumentNumber());
    assertEquals("Buyer", creditRequest.getCustomer().getFullName());
    assertEquals("ABC", creditRequest.getCustomer().getDocumentType());
    assertEquals("9278281280", creditRequest.getDisbursement().getAccountNumber());
    assertEquals("AH", creditRequest.getDisbursement().getAccountType());
    assertEquals("FIN", creditRequest.getAmortizationPaymentPeriodType().getCode());
    assertEquals("FIN", creditRequest.getInterestPayment().getCode());
    assertEquals("V", creditRequest.getInterestPayment().getGracePeriod().getCode());
    assertEquals("001", creditRequest.getInterestPayment().getGracePeriod().getInstallmentNumber());
  }

  private void assertTransactionsValues() {
    var transaction = singlePaymentRequestArgumentCaptor.getValue();

    assertEquals(MOCK_BGL_ACCOUNT, transaction.getDebtorAccount().getId().getOther().getId());
    assertEquals(
        "9278281281", transaction.getCreditorDetails().getAccount().getId().getOther().getId());
    assertEquals("9278281280", transaction.getRemittanceInformation().getInformation4());
  }

  private void assertEmailsValues() {
    var emailEvents = invoiceEmailInfoArgumentCaptor.getAllValues();
    assertEquals(seller.getFullName(), emailEvents.get(0).customerName());
    assertEquals(InvoiceEmailEvent.SETTLED.getValue(), emailEvents.get(0).action());
    assertEquals("Buyer", emailEvents.get(1).customerName());
    assertEquals(InvoiceEmailEvent.CREDITED.getValue(), emailEvents.get(1).action());
  }

  private InvoiceSettlementEventMessage buildMockSettlementMessage() {
    return InvoiceSettlementEventMessage.builder()
        .invoiceNumber("701-173-980660251--6243--4")
        .buyerIdentifier("B123")
        .sellerIdentifier("")
        .programme("P123")
        .masterRef("M123")
        .paymentCurrency("USD")
        .paymentAmount("10")
        .build();
  }
}
