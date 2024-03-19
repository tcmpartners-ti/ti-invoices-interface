package com.tcmp.tiapi.invoice.strategy.ticc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

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
import com.tcmp.tiapi.program.model.ProgramExtension;
import com.tcmp.tiapi.program.repository.ProgramExtensionRepository;
import com.tcmp.tiapi.ti.dto.request.AckServiceRequest;
import com.tcmp.tiapi.titoapigee.businessbanking.BusinessBankingService;
import com.tcmp.tiapi.titoapigee.businessbanking.dto.request.OperationalGatewayRequestPayload;
import com.tcmp.tiapi.titoapigee.businessbanking.dto.request.PayloadStatus;
import com.tcmp.tiapi.titoapigee.businessbanking.model.OperationalGatewayProcessCode;
import com.tcmp.tiapi.titoapigee.corporateloan.CorporateLoanService;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.request.DistributorCreditRequest;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.request.GracePeriod;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.request.InterestPayment;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.response.Data;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.response.DistributorCreditResponse;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.response.Error;
import com.tcmp.tiapi.titoapigee.operationalgateway.OperationalGatewayService;
import com.tcmp.tiapi.titoapigee.operationalgateway.model.InvoiceEmailEvent;
import com.tcmp.tiapi.titoapigee.operationalgateway.model.InvoiceEmailInfo;
import com.tcmp.tiapi.titoapigee.paymentexecution.PaymentExecutionService;
import com.tcmp.tiapi.titoapigee.paymentexecution.dto.request.TransactionRequest;
import com.tcmp.tiapi.titoapigee.paymentexecution.dto.request.TransactionType;
import com.tcmp.tiapi.titoapigee.paymentexecution.dto.response.BusinessAccountTransfersResponse;
import com.tcmp.tiapi.titoapigee.paymentexecution.dto.response.TransferResponseData;
import com.tcmp.tiapi.titoapigee.paymentexecution.exception.PaymentExecutionException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InvoiceFinancingFlowStrategyTest {
  @Mock private EventExtensionRepository eventExtensionRepository;
  @Mock private ProductMasterExtensionRepository productMasterExtensionRepository;
  @Mock private ProgramExtensionRepository programExtensionRepository;
  @Mock private CustomerRepository customerRepository;
  @Mock private AccountRepository accountRepository;
  @Mock private InvoiceRepository invoiceRepository;

  @Mock private CorporateLoanService corporateLoanService;
  @Mock private PaymentExecutionService paymentExecutionService;
  @Mock private OperationalGatewayService operationalGatewayService;
  @Mock private BusinessBankingService businessBankingService;

  @Captor private ArgumentCaptor<InvoiceEmailInfo> emailInfoArgumentCaptor;
  @Captor private ArgumentCaptor<DistributorCreditRequest> buyerCreditRequestArgumentCaptor;
  @Captor private ArgumentCaptor<DistributorCreditRequest> sellerCreditRequestArgumentCaptor;
  @Captor private ArgumentCaptor<TransactionRequest> transactionRequestArgumentCaptor;
  @Captor private ArgumentCaptor<OperationalGatewayRequestPayload> payloadArgumentCaptor;

  @InjectMocks private InvoiceFinancingFlowStrategy invoiceFinancingFlowStrategy;

  private Customer seller;

  @BeforeEach
  void setUp() {
    // !Note: TI saves string fields with extra spaces.
    Customer buyer =
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

    when(customerRepository.findFirstByIdMnemonic(anyString()))
        .thenReturn(Optional.of(buyer))
        .thenReturn(Optional.of(seller));
  }

  @Test
  void handleServiceRequest_itShouldNotifyIfCreditCreationFails() {
    var invoiceFinanceMessage = buildMockMessage();
    var failedBuyerCredit =
        new DistributorCreditResponse(
            Data.builder().error(new Error("ERR001", "Error", "ERROR")).build());

    when(productMasterExtensionRepository.findByMasterReference(anyString()))
        .thenReturn(
            Optional.of(ProductMasterExtension.builder().financeAccount("CC0974631820").build()));
    when(accountRepository.findByTypeAndCustomerMnemonic(anyString(), anyString()))
        .thenReturn(Optional.of(Account.builder().externalAccountNumber("AH0974631821").build()));
    when(invoiceRepository.findByProductMasterMasterReference(any()))
        .thenReturn(Optional.of(InvoiceMaster.builder().batchId("b123").build()));
    when(corporateLoanService.createCredit(any())).thenReturn(failedBuyerCredit);

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
  void handleServiceRequest_itShouldNotifyIfPaymentFails() {
    var invoiceFinanceMessage = buildMockMessage();
    var credit = new DistributorCreditResponse(Data.builder().error(Error.empty()).build());

    when(productMasterExtensionRepository.findByMasterReference(anyString()))
        .thenReturn(
            Optional.of(ProductMasterExtension.builder().financeAccount("CC0974631820").build()));
    when(accountRepository.findByTypeAndCustomerMnemonic(anyString(), anyString()))
        .thenReturn(Optional.of(Account.builder().externalAccountNumber("AH0974631821").build()));
    when(invoiceRepository.findByProductMasterMasterReference(any()))
        .thenReturn(Optional.of(InvoiceMaster.builder().batchId("b123").build()));
    when(corporateLoanService.createCredit(any())).thenReturn(credit);
    when(paymentExecutionService.makeTransactionRequest(any()))
        .thenThrow(new PaymentExecutionException("Transaction failed"));

    invoiceFinancingFlowStrategy.handleServiceRequest(
        new AckServiceRequest<>(null, invoiceFinanceMessage));

    verify(operationalGatewayService).sendNotificationRequest(emailInfoArgumentCaptor.capture());
    verify(corporateLoanService).createCredit(any(DistributorCreditRequest.class));
    verify(businessBankingService)
        .notifyEvent(any(OperationalGatewayProcessCode.class), payloadArgumentCaptor.capture());

    assertEquals(PayloadStatus.FAILED.getValue(), payloadArgumentCaptor.getValue().status());
  }

  @Test
  void handleServiceRequest_itShouldHandleHappyPath() {
    FinanceAckMessage invoiceFinanceMessage = buildMockMessage();
    ProgramExtension programExtension =
        ProgramExtension.builder().requiresExtraFinancing(true).extraFinancingDays(6).build();
    DistributorCreditResponse buyerCredit =
        new DistributorCreditResponse(
            Data.builder().disbursementAmount(100).error(Error.empty()).build());
    DistributorCreditResponse sellerCredit =
        new DistributorCreditResponse(
            Data.builder()
                .disbursementAmount(100)
                .totalInstallmentsAmount(110)
                .error(Error.empty())
                .build());

    when(productMasterExtensionRepository.findByMasterReference(anyString()))
        .thenReturn(
            Optional.of(ProductMasterExtension.builder().financeAccount("CC0974631820").build()));
    when(accountRepository.findByTypeAndCustomerMnemonic(anyString(), anyString()))
        .thenReturn(Optional.of(Account.builder().externalAccountNumber("AH0974631821").build()));
    when(invoiceRepository.findByProductMasterMasterReference(any()))
        .thenReturn(Optional.of(InvoiceMaster.builder().batchId("b123").build()));
    when(programExtensionRepository.findByProgrammeId(anyString()))
        .thenReturn(Optional.of(programExtension));
    when(corporateLoanService.createCredit(any())).thenReturn(buyerCredit);
    when(corporateLoanService.simulateCredit(any())).thenReturn(sellerCredit);
    when(paymentExecutionService.makeTransactionRequest(any()))
        .thenReturn(new BusinessAccountTransfersResponse(new TransferResponseData("OK", "", "")))
        .thenReturn(new BusinessAccountTransfersResponse(new TransferResponseData("OK", "", "")));

    invoiceFinancingFlowStrategy.handleServiceRequest(
        new AckServiceRequest<>(null, invoiceFinanceMessage));

    verify(operationalGatewayService, times(2))
        .sendNotificationRequest(emailInfoArgumentCaptor.capture());
    verify(corporateLoanService).createCredit(buyerCreditRequestArgumentCaptor.capture());
    verify(corporateLoanService).simulateCredit(sellerCreditRequestArgumentCaptor.capture());
    verify(paymentExecutionService, times(4))
        .makeTransactionRequest(transactionRequestArgumentCaptor.capture());
    verify(businessBankingService)
        .notifyEvent(any(OperationalGatewayProcessCode.class), payloadArgumentCaptor.capture());

    assertEmailsAreMappedCorrectly();
    assertCreditsAreMappedCorrectly();
    assertTransactionsAreMappedCorrectly();
  }

  private void assertEmailsAreMappedCorrectly() {
    var expectedAmount = new BigDecimal("100.00");
    InvoiceEmailInfo actualFirstEmail = emailInfoArgumentCaptor.getAllValues().get(0);
    InvoiceEmailInfo actualSecondEmail = emailInfoArgumentCaptor.getAllValues().get(1);

    assertEquals(seller.getFullName(), actualFirstEmail.customerName());
    assertEquals(InvoiceEmailEvent.FINANCED.getValue(), actualFirstEmail.action());
    assertEquals(expectedAmount, actualFirstEmail.amount());

    assertEquals(seller.getFullName(), actualSecondEmail.customerName());
    assertEquals(InvoiceEmailEvent.PROCESSED.getValue(), actualSecondEmail.action());
    assertEquals(expectedAmount, actualSecondEmail.amount());
  }

  private void assertCreditsAreMappedCorrectly() {
    var expectedInterestPayment = new InterestPayment("FIN", new GracePeriod("V", "001"));
    var actualBuyerCreditRequest = buyerCreditRequestArgumentCaptor.getValue();
    var actualSellerCreditRequest = sellerCreditRequestArgumentCaptor.getValue();

    assertNotNull(actualBuyerCreditRequest);
    assertNotNull(actualBuyerCreditRequest.customer());
    assertEquals("1722466420002", actualBuyerCreditRequest.customer().documentNumber());
    assertEquals("Buyer", actualBuyerCreditRequest.customer().fullName());
    assertEquals("0003", actualBuyerCreditRequest.customer().documentType());
    assertEquals("D", actualBuyerCreditRequest.termPeriodType().code());
    assertEquals("FIN", actualBuyerCreditRequest.amortizationPaymentPeriodType().code());
    assertEquals(expectedInterestPayment, actualBuyerCreditRequest.interestPayment());
    assertEquals("C99", actualBuyerCreditRequest.maturityForm());
    assertNotNull(actualBuyerCreditRequest.disbursement());
    assertNotNull(actualBuyerCreditRequest.effectiveDate());
    assertEquals(30, actualBuyerCreditRequest.term());
    assertEquals(24, actualSellerCreditRequest.term());
    assertEquals(
        "001", actualBuyerCreditRequest.interestPayment().gracePeriod().installmentNumber());
    assertEquals(PayloadStatus.SUCCEEDED.getValue(), payloadArgumentCaptor.getValue().status());
  }

  private void assertTransactionsAreMappedCorrectly() {
    var expectedBuyerAccount = "0974631820";
    var expectedSellerAccount = "0974631821";
    var buyerToBglTransaction = transactionRequestArgumentCaptor.getAllValues().get(0);
    var bglToSellerTransaction = transactionRequestArgumentCaptor.getAllValues().get(1);
    var sellerToBglTransaction = transactionRequestArgumentCaptor.getAllValues().get(2);
    var bglToBuyerTransaction = transactionRequestArgumentCaptor.getAllValues().get(3);

    assertEquals(TransactionType.CLIENT_TO_BGL.getValue(), buyerToBglTransaction.transactionType());
    assertEquals(expectedBuyerAccount, buyerToBglTransaction.debtor().account().accountId());
    assertEquals(
        TransactionType.BGL_TO_CLIENT.getValue(), bglToSellerTransaction.transactionType());
    assertEquals(expectedSellerAccount, bglToSellerTransaction.creditor().account().accountId());
    assertEquals(
        TransactionType.CLIENT_TO_BGL.getValue(), sellerToBglTransaction.transactionType());
    assertEquals(expectedSellerAccount, sellerToBglTransaction.debtor().account().accountId());
    assertEquals(TransactionType.BGL_TO_CLIENT.getValue(), bglToBuyerTransaction.transactionType());
    assertEquals(expectedBuyerAccount, bglToBuyerTransaction.creditor().account().accountId());
  }

  private FinanceAckMessage buildMockMessage() {
    return FinanceAckMessage.builder()
        .programme("P123")
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
