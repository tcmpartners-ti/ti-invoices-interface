package com.tcmp.tiapi.invoice.strategy.ticc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcmp.tiapi.customer.model.Account;
import com.tcmp.tiapi.customer.model.Address;
import com.tcmp.tiapi.customer.model.Customer;
import com.tcmp.tiapi.customer.model.CustomerId;
import com.tcmp.tiapi.customer.repository.AccountRepository;
import com.tcmp.tiapi.customer.repository.CustomerRepository;
import com.tcmp.tiapi.invoice.dto.ti.financeack.FinanceAckMessage;
import com.tcmp.tiapi.invoice.dto.ti.financeack.FinancePaymentDetails;
import com.tcmp.tiapi.invoice.dto.ti.financeack.Invoice;
import com.tcmp.tiapi.invoice.model.InvoiceMaster;
import com.tcmp.tiapi.invoice.model.ProductMasterExtension;
import com.tcmp.tiapi.invoice.repository.EventExtensionRepository;
import com.tcmp.tiapi.invoice.repository.InvoiceRepository;
import com.tcmp.tiapi.invoice.repository.ProductMasterExtensionRepository;
import com.tcmp.tiapi.invoice.repository.redis.BulkCreateInvoicesFileInfoRepository;
import com.tcmp.tiapi.invoice.service.files.realoutput.InvoiceRealOutputFileUploader;
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
import com.tcmp.tiapi.titoapigee.corporateloan.dto.request.*;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.response.Amortization;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.response.Data;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.response.DistributorCreditResponse;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.response.Error;
import com.tcmp.tiapi.titoapigee.corporateloan.exception.CreditCreationException;
import com.tcmp.tiapi.titoapigee.operationalgateway.OperationalGatewayService;
import com.tcmp.tiapi.titoapigee.operationalgateway.model.InvoiceEmailEvent;
import com.tcmp.tiapi.titoapigee.operationalgateway.model.InvoiceEmailInfo;
import com.tcmp.tiapi.titofcm.dto.SinglePaymentMapper;
import com.tcmp.tiapi.titofcm.dto.request.*;
import com.tcmp.tiapi.titofcm.dto.response.PaymentResultResponse;
import com.tcmp.tiapi.titofcm.dto.response.SinglePaymentResponse;
import com.tcmp.tiapi.titofcm.model.InvoicePaymentCorrelationInfo;
import com.tcmp.tiapi.titofcm.repository.InvoicePaymentCorrelationInfoRepository;
import com.tcmp.tiapi.titofcm.service.SingleElectronicPaymentService;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class InvoiceFinancingFlowStrategyTest {
  @Mock private UUIDGenerator uuidGenerator;
  @Mock private ObjectMapper objectMapper;

  @Mock private AccountRepository accountRepository;
  @Mock private CustomerRepository customerRepository;
  @Mock private EventExtensionRepository eventExtensionRepository;
  @Mock private BulkCreateInvoicesFileInfoRepository createInvoicesFileInfoRepository;
  @Mock private InvoiceRealOutputFileUploader realOutputFileUploader;
  @Mock private InvoicePaymentCorrelationInfoRepository invoicePaymentCorrelationInfoRepository;
  @Mock private InvoiceRepository invoiceRepository;
  @Mock private ProductMasterExtensionRepository productMasterExtensionRepository;
  @Mock private ProgramExtensionRepository programExtensionRepository;

  @Mock private SingleElectronicPaymentService singleElectronicPaymentService;
  @Mock private CorporateLoanService corporateLoanService;
  @Mock private OperationalGatewayService operationalGatewayService;
  @Mock private BusinessBankingService businessBankingService;

  @Captor private ArgumentCaptor<InvoiceEmailInfo> emailInfoArgumentCaptor;
  @Captor private ArgumentCaptor<DistributorCreditRequest> creditRequestArgumentCaptor;
  @Captor private ArgumentCaptor<SinglePaymentRequest> singlePaymentRequestArgumentCaptor;
  @Captor private ArgumentCaptor<OperationalGatewayRequestPayload> payloadArgumentCaptor;
  @Captor private ArgumentCaptor<InvoicePaymentCorrelationInfo> invoicePaymentInfoArgumentCaptor;

  private InvoiceFinancingFlowStrategy invoiceFinancingFlowStrategy;

  private Customer buyer;
  private Customer seller;
  private ProductMasterExtension invoiceExtension;
  private DistributorCreditResponse buyerCredit;
  private DistributorCreditResponse sellerCredit;

  @BeforeEach
  void setUp() {
    var zoneId = ZoneId.of("America/Guayaquil");
    var mockedToday = LocalDate.of(2024, 2, 8);
    var mockedClock = Clock.fixed(mockedToday.atStartOfDay(zoneId).toInstant(), zoneId);

    var singlePaymentMapper = Mappers.getMapper(SinglePaymentMapper.class);
    var corporateLoanMapper = Mappers.getMapper(CorporateLoanMapper.class);
    ReflectionTestUtils.setField(singlePaymentMapper, "clock", mockedClock);

    invoiceFinancingFlowStrategy =
        new InvoiceFinancingFlowStrategy(
            uuidGenerator,
            objectMapper,
            mockedClock,
            accountRepository,
            customerRepository,
            eventExtensionRepository,
            createInvoicesFileInfoRepository,
            realOutputFileUploader,
            invoicePaymentCorrelationInfoRepository,
            invoiceRepository,
            productMasterExtensionRepository,
            programExtensionRepository,
            corporateLoanMapper,
            singlePaymentMapper,
            singleElectronicPaymentService,
            corporateLoanService,
            operationalGatewayService,
            businessBankingService);

    // !Note: TI saves string fields with extra spaces.
    buyer =
        Customer.builder()
            .id(
                CustomerId.builder()
                    .sourceBankingBusinessCode("BPEC")
                    .mnemonic("1722466420002               ")
                    .build())
            .type("ADL")
            .number("1244188")
            .fullName("Buyer                              ")
            .bankCode1("0003                              ")
            .build();
    seller =
        Customer.builder()
            .fullName("Seller")
            .address(Address.builder().customerEmail("seller@mail.com").build())
            .build();
    invoiceExtension =
        ProductMasterExtension.builder()
            .financeAccount("CC0974631820")
            .fileCreationUuid("       ")
            .build();
    buyerCredit =
        new DistributorCreditResponse(
            Data.builder()
                .operationId("Credit123")
                .interestRate(1)
                .disbursementAmount(12.34)
                .tax(
                    com.tcmp.tiapi.titoapigee.corporateloan.dto.response.Tax.builder()
                        .factor(1.1)
                        .amount(123)
                        .build())
                .disbursementAmount(100)
                .amortizations(
                    List.of(
                        new Amortization("", "IV", "12.04"), new Amortization("", "AS", "1.04")))
                .error(Error.empty())
                .build());
  }

  @Test
  void handleServiceRequest_itShouldNotifyIfCreditCreationFails() {
    var invoiceFinanceMessage = buildMockMessage();
    var failedBuyerCredit =
        new DistributorCreditResponse(
            Data.builder().error(new Error("ERR001", "Error", "ERROR")).build());

    when(productMasterExtensionRepository.findByMasterReference(anyString()))
        .thenReturn(Optional.of(invoiceExtension));
    when(accountRepository.findByTypeAndCustomerMnemonic(anyString(), anyString()))
        .thenReturn(Optional.of(Account.builder().externalAccountNumber("AH0974631821").build()));
    when(invoiceRepository.findByProductMasterMasterReference(any()))
        .thenReturn(Optional.of(InvoiceMaster.builder().batchId("b123").build()));
    when(corporateLoanService.createCredit(any())).thenReturn(failedBuyerCredit);
    when(customerRepository.findFirstByIdMnemonic(anyString()))
        .thenReturn(Optional.of(buyer))
        .thenReturn(Optional.of(seller));

    invoiceFinancingFlowStrategy.handleServiceRequest(
        new AckServiceRequest<>(null, invoiceFinanceMessage));

    verify(operationalGatewayService).sendNotificationRequest(emailInfoArgumentCaptor.capture());
    String actualSellerEmail = emailInfoArgumentCaptor.getValue().customerEmail();
    assertEquals(seller.getAddress().getCustomerEmail(), actualSellerEmail);

    verify(corporateLoanService).createCredit(any(DistributorCreditRequest.class));
    verify(businessBankingService)
        .notifyEvent(any(OperationalGatewayProcessCode.class), payloadArgumentCaptor.capture());
    assertEquals(PayloadStatus.FAILED.getValue(), payloadArgumentCaptor.getValue().status());
  }

  @Test
  void handleServiceRequest_itShouldNotifyIfPaymentRequestFails() {
    var invoiceFinanceMessage = buildMockMessage();

    when(customerRepository.findFirstByIdMnemonic(anyString()))
        .thenReturn(Optional.of(buyer))
        .thenReturn(Optional.of(seller));
    when(productMasterExtensionRepository.findByMasterReference(anyString()))
        .thenReturn(Optional.of(invoiceExtension));
    when(accountRepository.findByTypeAndCustomerMnemonic(anyString(), anyString()))
        .thenReturn(Optional.of(Account.builder().externalAccountNumber("AH0974631821").build()));
    when(invoiceRepository.findByProductMasterMasterReference(any()))
        .thenReturn(Optional.of(InvoiceMaster.builder().batchId("b123").build()));
    when(corporateLoanService.createCredit(any()))
        .thenThrow(new CreditCreationException("Credit creation failed."));

    invoiceFinancingFlowStrategy.handleServiceRequest(
        new AckServiceRequest<>(null, invoiceFinanceMessage));

    verify(operationalGatewayService).sendNotificationRequest(emailInfoArgumentCaptor.capture());
    verify(corporateLoanService).createCredit(any(DistributorCreditRequest.class));
    verify(businessBankingService)
        .notifyEvent(any(OperationalGatewayProcessCode.class), payloadArgumentCaptor.capture());

    assertEquals(PayloadStatus.FAILED.getValue(), payloadArgumentCaptor.getValue().status());
  }

  /** This test executes all the flow (as it should work in runtime) */
  @Test
  void itShouldHandleHappyPath() {
    String correlationInfoUuid = "001-001-001";
    String paymentReference = "ref123";

    FinanceAckMessage invoiceFinanceMessage = buildMockMessage();
    ProgramExtension programExtension =
        ProgramExtension.builder().requiresExtraFinancing(true).extraFinancingDays(6).build();

    when(customerRepository.findFirstByIdMnemonic(anyString()))
        .thenReturn(Optional.of(buyer))
        .thenReturn(Optional.of(seller));
    when(productMasterExtensionRepository.findByMasterReference(anyString()))
        .thenReturn(Optional.of(invoiceExtension));
    when(accountRepository.findByTypeAndCustomerMnemonic(anyString(), anyString()))
        .thenReturn(Optional.of(Account.builder().externalAccountNumber("AH0974631821").build()));
    when(invoiceRepository.findByProductMasterMasterReference(any()))
        .thenReturn(
            Optional.of(InvoiceMaster.builder().reference("INV123").batchId("b123").build()));
    when(programExtensionRepository.findByProgrammeId(anyString()))
        .thenReturn(Optional.of(programExtension));
    when(corporateLoanService.createCredit(any())).thenReturn(buyerCredit);
    when(corporateLoanService.simulateCredit(any())).thenReturn(sellerCredit);
    when(uuidGenerator.getNewId()).thenReturn(correlationInfoUuid);
    when(singleElectronicPaymentService.createSinglePayment(any()))
        .thenReturn(new SinglePaymentResponse(new SinglePaymentResponse.Data(paymentReference)));

    var serviceRequest = new AckServiceRequest<>(null, invoiceFinanceMessage);
    var creditPaymentInfo =
        InvoicePaymentCorrelationInfo.builder()
            .id("abc-123")
            .paymentReference("REF")
            .initialEvent(InvoicePaymentCorrelationInfo.InitialEvent.BUYER_CENTRIC_FINANCE_0)
            .build();
    var creditPaymentResult =
        new PaymentResultResponse(
            PaymentResultResponse.Status.SUCCEEDED,
            "REF123",
            PaymentResultResponse.Type.BGL_CLIENT);
    var taxesPaymentInfo =
        InvoicePaymentCorrelationInfo.builder()
            .id("abc-123")
            .paymentReference("REF")
            .initialEvent(InvoicePaymentCorrelationInfo.InitialEvent.BUYER_CENTRIC_FINANCE_1)
            .build();
    var taxesPaymentResult =
        new PaymentResultResponse(
            PaymentResultResponse.Status.SUCCEEDED,
            "REF456",
            PaymentResultResponse.Type.BGL_CLIENT);

    // Call every method of the flow
    invoiceFinancingFlowStrategy.handleServiceRequest(serviceRequest);
    invoiceFinancingFlowStrategy.handleCreditPaymentResult(
        invoiceFinanceMessage, creditPaymentResult, creditPaymentInfo);
    invoiceFinancingFlowStrategy.handleTaxesPaymentResult(
        invoiceFinanceMessage, taxesPaymentResult, taxesPaymentInfo);

    verify(operationalGatewayService, times(2))
        .sendNotificationRequest(emailInfoArgumentCaptor.capture());
    verify(corporateLoanService).createCredit(creditRequestArgumentCaptor.capture());
    verify(corporateLoanService).simulateCredit(any(DistributorCreditRequest.class));
    verify(singleElectronicPaymentService)
        .createSinglePayment(singlePaymentRequestArgumentCaptor.capture());
    verify(invoicePaymentCorrelationInfoRepository)
        .save(invoicePaymentInfoArgumentCaptor.capture());

    verify(businessBankingService)
        .notifyEvent(any(OperationalGatewayProcessCode.class), payloadArgumentCaptor.capture());
    verify(invoicePaymentCorrelationInfoRepository)
        .delete(invoicePaymentInfoArgumentCaptor.capture());

    var expectedInitialEvent = InvoicePaymentCorrelationInfo.InitialEvent.BUYER_CENTRIC_FINANCE_1;
    var actualInitialEvent = invoicePaymentInfoArgumentCaptor.getValue().getInitialEvent();
    assertEquals(expectedInitialEvent, actualInitialEvent);

    var actualPaymentReference = invoicePaymentInfoArgumentCaptor.getValue().getPaymentReference();
    assertEquals("REF", actualPaymentReference);

    assertFinancedEmailIsMappedCorrectly();
    assertCreditsAreMappedCorrectly();
    assertPaymentIsMappedCorrectly();

    assertEquals("abc-123", invoicePaymentInfoArgumentCaptor.getValue().getId());
    assertEquals("REF", invoicePaymentInfoArgumentCaptor.getValue().getPaymentReference());
  }

  private void assertFinancedEmailIsMappedCorrectly() {
    var expectedAmount = new BigDecimal("100.00");
    var actualFinancedEmail = emailInfoArgumentCaptor.getAllValues().get(0);
    var actualProcessedEmail = emailInfoArgumentCaptor.getAllValues().get(1);

    assertEquals(seller.getFullName(), actualProcessedEmail.customerName());
    assertEquals(InvoiceEmailEvent.FINANCED.getValue(), actualFinancedEmail.action());
    assertEquals(InvoiceEmailEvent.PROCESSED.getValue(), actualProcessedEmail.action());
    assertEquals(expectedAmount, actualProcessedEmail.amount());
  }

  private void assertCreditsAreMappedCorrectly() {
    var actualBuyerCreditRequest = creditRequestArgumentCaptor.getValue();
    var expectedBuyerCreditRequest =
        DistributorCreditRequest.builder()
            .customer(
                com.tcmp.tiapi.titoapigee.corporateloan.dto.request.Customer.builder()
                    .customerId("1244188")
                    .documentType("0003")
                    .documentNumber("1722466420002")
                    .fullName("Buyer")
                    .build())
            .disbursement(
                Disbursement.builder()
                    .accountNumber("0974631820")
                    .accountType("CC")
                    .bankId("010")
                    .form("N/C")
                    .build())
            .amount(new BigDecimal("100.00"))
            .effectiveDate("2023-11-06")
            .commercialTrade(new CommercialTrade("ADL"))
            .termPeriodType(new TermPeriodType("D"))
            .amortizationPaymentPeriodType(new AmortizationPaymentPeriodType("FIN"))
            .interestPayment(new InterestPayment("FIN", new GracePeriod("V", "001")))
            .maturityForm("C99")
            .term(30)
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

    assertEquals(expectedBuyerCreditRequest, actualBuyerCreditRequest);
  }

  private void assertPaymentIsMappedCorrectly() {
    var expectedPaymentRequest =
        SinglePaymentRequest.builder()
            .legalEntity("ECU")
            .paymentReference("01-001")
            .transactionType("CREDIT")
            .paymentbankproduct("Cadenas")
            .methodOfPayment("TI")
            .requestedExecutionDate(OffsetDateTime.parse("2024-02-08T00:00-05:00"))
            .requestedExecutionTime(OffsetDateTime.parse("2024-02-08T00:00-05:00"))
            .isConfidentialPayment(false)
            .chargeBearer("OUR")
            .debtorAccount(
                com.tcmp.tiapi.titofcm.dto.request.Account.builder()
                    .id(new ID(new Other(null)))
                    .type("Savings Account")
                    .currency(null)
                    .name("BGL")
                    .build())
            .instructedAmount(
                new InstructedAmountCurrencyOfTransfer2(null, new BigDecimal("100.00")))
            .creditorDetails(
                new CreditorDetails(
                    "Seller",
                    com.tcmp.tiapi.titofcm.dto.request.Account.builder()
                        .id(new ID(new Other("0974631821")))
                        .type("Savings Account")
                        .build()))
            .creditorAgent(
                CreditorAgent.builder()
                    .identifierType("NCC")
                    .otherId("0010")
                    .name("Banco Pichincha Ecuador")
                    .postalAddress(new PostalAddress(List.of(), "ADDR", "EC"))
                    .build())
            .remittanceInformation(
                new RemittanceInformation(
                    "Debito Fact 701-173-980660251 null",
                    "Pago Fact 701-173-980660251 null",
                    "0974631820"))
            .build();

    assertEquals(expectedPaymentRequest, singlePaymentRequestArgumentCaptor.getValue());
  }

  @Test
  void handleCreditPaymentResult_itShouldNotifyIfPaymentFailed() {
    var message = buildMockMessage();
    var paymentResult =
        PaymentResultResponse.builder()
            .paymentReference("ref123")
            .status(PaymentResultResponse.Status.FAILED)
            .type(PaymentResultResponse.Type.BGL_CLIENT)
            .build();
    var invoicePaymentInfo = InvoicePaymentCorrelationInfo.builder().build();

    when(invoiceRepository.findByProductMasterMasterReference(anyString()))
        .thenReturn(Optional.of(InvoiceMaster.builder().batchId("b123").build()));
    when(productMasterExtensionRepository.findByMasterReference(anyString()))
        .thenReturn(Optional.of(ProductMasterExtension.builder().fileCreationUuid("    ").build()));
    doNothing().when(invoicePaymentCorrelationInfoRepository).deleteByPaymentReference(anyString());

    invoiceFinancingFlowStrategy.handleCreditPaymentResult(
        message, paymentResult, invoicePaymentInfo);

    verify(businessBankingService).notifyEvent(any(), payloadArgumentCaptor.capture());
    assertEquals(PayloadStatus.FAILED.getValue(), payloadArgumentCaptor.getValue().status());
  }

  private FinanceAckMessage buildMockMessage() {
    return FinanceAckMessage.builder()
        .programme("P123")
        .masterRef("123")
        .theirRef("701-173-980660251--6243--4")
        .buyerIdentifier("B123")
        .sellerIdentifier("S123")
        .financeDealCurrency("USD")
        .financeDealAmount("10000")
        .startDate("2023-11-06")
        .maturityDate("2023-11-30")
        .paymentDetails(FinancePaymentDetails.builder().currency("USD").build())
        .invoiceArray(List.of(Invoice.builder().invoiceReference("01-001").build()))
        .build();
  }
}
