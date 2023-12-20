package com.tcmp.tiapi.invoice.strategy.ticc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
import com.tcmp.tiapi.ti.dto.request.AckServiceRequest;
import com.tcmp.tiapi.titoapigee.businessbanking.BusinessBankingService;
import com.tcmp.tiapi.titoapigee.businessbanking.dto.request.OperationalGatewayRequestPayload;
import com.tcmp.tiapi.titoapigee.businessbanking.dto.request.PayloadStatus;
import com.tcmp.tiapi.titoapigee.businessbanking.model.OperationalGatewayProcessCode;
import com.tcmp.tiapi.titoapigee.corporateloan.CorporateLoanService;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.request.DistributorCreditRequest;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.response.Data;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.response.DistributorCreditResponse;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.response.Error;
import com.tcmp.tiapi.titoapigee.operationalgateway.OperationalGatewayService;
import com.tcmp.tiapi.titoapigee.operationalgateway.model.InvoiceEmailEvent;
import com.tcmp.tiapi.titoapigee.operationalgateway.model.InvoiceEmailInfo;
import com.tcmp.tiapi.titoapigee.paymentexecution.PaymentExecutionService;
import com.tcmp.tiapi.titoapigee.paymentexecution.dto.response.BusinessAccountTransfersResponse;
import com.tcmp.tiapi.titoapigee.paymentexecution.dto.response.TransferResponseData;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InvoiceSettlementFlowStrategyTest {
  @Mock private CorporateLoanService corporateLoanService;
  @Mock private PaymentExecutionService paymentExecutionService;
  @Mock private OperationalGatewayService operationalGatewayService;
  @Mock private BusinessBankingService businessBankingService;

  @Mock private AccountRepository accountRepository;
  @Mock private CustomerRepository customerRepository;
  @Mock private InvoiceRepository invoiceRepository;
  @Mock private ProductMasterExtensionRepository productMasterExtensionRepository;
  @Mock private ProgramExtensionRepository programExtensionRepository;

  @Captor private ArgumentCaptor<InvoiceEmailInfo> invoiceEmailInfoArgumentCaptor;
  @Captor private ArgumentCaptor<OperationalGatewayRequestPayload> requestPayloadArgumentCaptor;

  private InvoiceSettlementFlowStrategy invoiceSettlementFlowStrategy;

  private Customer buyer;
  private Customer seller;

  @BeforeEach
  void setUp() throws NoSuchFieldException, IllegalAccessException {
    invoiceSettlementFlowStrategy =
        new InvoiceSettlementFlowStrategy(
            corporateLoanService,
            paymentExecutionService,
            operationalGatewayService,
            businessBankingService,
            accountRepository,
            customerRepository,
            invoiceRepository,
            productMasterExtensionRepository,
            programExtensionRepository);
    // Mock injected fields
    Field activeProfile = InvoiceSettlementFlowStrategy.class.getDeclaredField("activeProfile");
    Field bglAccount = InvoiceSettlementFlowStrategy.class.getDeclaredField("bglAccount");
    activeProfile.setAccessible(true);
    bglAccount.setAccessible(true);
    activeProfile.set(invoiceSettlementFlowStrategy, "prod");
    bglAccount.set(invoiceSettlementFlowStrategy, "123");

    buyer =
        Customer.builder()
            .id(CustomerId.builder().mnemonic("1722466420").build())
            .bankCode1("ABC")
            .type("B")
            .number("123")
            .fullName("Buyer")
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
    when(productMasterExtensionRepository.findByMasterId(anyLong()))
        .thenReturn(
            Optional.of(ProductMasterExtension.builder().financeAccount("AH9278281280").build()));

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
    when(productMasterExtensionRepository.findByMasterId(anyLong()))
        .thenReturn(
            Optional.of(ProductMasterExtension.builder().financeAccount("AH9278281280").build()));
    when(corporateLoanService.createCredit(any(DistributorCreditRequest.class)))
        .thenReturn(
            new DistributorCreditResponse(
                Data.builder().error(new Error("123", "Invalid credit.", "ERROR")).build()));

    invoiceSettlementFlowStrategy.handleServiceRequest(
        new AckServiceRequest<>(null, buildMockSettlementMessage()));

    verify(corporateLoanService).createCredit(any(DistributorCreditRequest.class));
    verify(businessBankingService)
        .notifyEvent(
            eq(OperationalGatewayProcessCode.INVOICE_SETTLEMENT),
            requestPayloadArgumentCaptor.capture());
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
    when(productMasterExtensionRepository.findByMasterId(anyLong()))
        .thenReturn(
            Optional.of(ProductMasterExtension.builder().financeAccount("AH9278281280").build()));
    when(accountRepository.findByTypeAndCustomerMnemonic(anyString(), anyString()))
        .thenReturn(Optional.of(Account.builder().externalAccountNumber("AH9278281281").build()));
    when(corporateLoanService.createCredit(any()))
        .thenReturn(
            new DistributorCreditResponse(Data.builder().error(new Error("", "", "INFO")).build()));
    when(paymentExecutionService.makeTransactionRequest(any()))
        .thenReturn(
            new BusinessAccountTransfersResponse(
                TransferResponseData.builder().status("OK").build()))
        .thenReturn(
            new BusinessAccountTransfersResponse(
                TransferResponseData.builder().status("NOT_OK").build()));

    invoiceSettlementFlowStrategy.handleServiceRequest(
        new AckServiceRequest<>(null, buildMockSettlementMessage()));

    verify(corporateLoanService).createCredit(any());
    verify(businessBankingService)
        .notifyEvent(
            eq(OperationalGatewayProcessCode.INVOICE_SETTLEMENT),
            requestPayloadArgumentCaptor.capture());

    var payload = requestPayloadArgumentCaptor.getValue();
    assertEquals(PayloadStatus.FAILED.getValue(), payload.status());
  }

  @Test
  void
      handleServiceRequest_itShouldSendBuyerNotificationAndSellerNotificationsIfExtraFinancedAndHasNotBeenFinanced() {
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
    when(productMasterExtensionRepository.findByMasterId(anyLong()))
        .thenReturn(
            Optional.of(ProductMasterExtension.builder().financeAccount("AH9278281280").build()));
    when(accountRepository.findByTypeAndCustomerMnemonic(anyString(), anyString()))
        .thenReturn(Optional.of(Account.builder().externalAccountNumber("AH9278281281").build()));
    when(corporateLoanService.createCredit(any()))
        .thenReturn(
            new DistributorCreditResponse(Data.builder().error(new Error("", "", "INFO")).build()));
    when(paymentExecutionService.makeTransactionRequest(any()))
        .thenReturn(
            new BusinessAccountTransfersResponse(
                TransferResponseData.builder().status("OK").build()));

    invoiceSettlementFlowStrategy.handleServiceRequest(
        new AckServiceRequest<>(null, buildMockSettlementMessage()));

    verify(corporateLoanService).createCredit(any());
    verify(paymentExecutionService, times(2)).makeTransactionRequest(any());

    verify(operationalGatewayService, times(3))
        .sendNotificationRequest(invoiceEmailInfoArgumentCaptor.capture());
    verify(businessBankingService)
        .notifyEvent(eq(OperationalGatewayProcessCode.INVOICE_SETTLEMENT), any());

    var emailEvents = invoiceEmailInfoArgumentCaptor.getAllValues();

    assertEquals(seller.getFullName(), emailEvents.get(0).customerName());
    assertEquals(InvoiceEmailEvent.SETTLED.getValue(), emailEvents.get(0).action());

    assertEquals(buyer.getFullName(), emailEvents.get(1).customerName());
    assertEquals(InvoiceEmailEvent.CREDITED.getValue(), emailEvents.get(1).action());

    assertEquals(seller.getFullName(), emailEvents.get(2).customerName());
    assertEquals(InvoiceEmailEvent.PROCESSED.getValue(), emailEvents.get(2).action());
  }

  private InvoiceSettlementEventMessage buildMockSettlementMessage() {
    return InvoiceSettlementEventMessage.builder()
        .buyerIdentifier("B123")
        .sellerIdentifier("")
        .programme("P123")
        .masterRef("M123")
        .paymentCurrency("USD")
        .paymentAmount("10")
        .build();
  }
}