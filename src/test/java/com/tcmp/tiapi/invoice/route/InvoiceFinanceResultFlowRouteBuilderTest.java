package com.tcmp.tiapi.invoice.route;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.tcmp.tiapi.customer.model.Account;
import com.tcmp.tiapi.customer.model.Customer;
import com.tcmp.tiapi.invoice.dto.ti.financeack.FinanceAckMessage;
import com.tcmp.tiapi.invoice.dto.ti.financeack.Invoice;
import com.tcmp.tiapi.invoice.model.InvoiceMaster;
import com.tcmp.tiapi.invoice.model.ProductMasterExtension;
import com.tcmp.tiapi.invoice.service.InvoiceFinancingService;
import com.tcmp.tiapi.messaging.model.requests.AckServiceRequest;
import com.tcmp.tiapi.titoapigee.businessbanking.BusinessBankingService;
import com.tcmp.tiapi.titoapigee.businessbanking.dto.request.OperationalGatewayRequestPayload;
import com.tcmp.tiapi.titoapigee.businessbanking.dto.request.PayloadStatus;
import com.tcmp.tiapi.titoapigee.businessbanking.model.OperationalGatewayProcessCode;
import com.tcmp.tiapi.titoapigee.corporateloan.CorporateLoanService;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.response.Data;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.response.DistributorCreditResponse;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.response.Error;
import com.tcmp.tiapi.titoapigee.corporateloan.exception.CreditCreationException;
import com.tcmp.tiapi.titoapigee.operationalgateway.OperationalGatewayService;
import com.tcmp.tiapi.titoapigee.operationalgateway.model.InvoiceEmailEvent;
import com.tcmp.tiapi.titoapigee.operationalgateway.model.InvoiceEmailInfo;
import com.tcmp.tiapi.titoapigee.paymentexecution.PaymentExecutionService;
import com.tcmp.tiapi.titoapigee.paymentexecution.dto.response.BusinessAccountTransfersResponse;
import com.tcmp.tiapi.titoapigee.paymentexecution.dto.response.TransferResponseData;
import java.math.BigDecimal;
import java.util.List;
import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InvoiceFinanceResultFlowRouteBuilderTest extends CamelTestSupport {
  private static final String URI_FROM = "direct:startFinanceFlow";

  @Mock private InvoiceFinancingService invoiceFinancingService;
  @Mock private CorporateLoanService corporateLoanService;
  @Mock private PaymentExecutionService paymentExecutionService;
  @Mock private OperationalGatewayService operationalGatewayService;
  @Mock private BusinessBankingService businessBankingService;

  @Captor ArgumentCaptor<Customer> customerArgumentCaptor;
  @Captor ArgumentCaptor<InvoiceEmailEvent> emailEventArgumentCaptor;
  @Captor ArgumentCaptor<BigDecimal> amountArgumentCaptor;
  @Captor ArgumentCaptor<OperationalGatewayProcessCode> operationalGatewayProcessCodeArgumentCaptor;

  @Captor
  ArgumentCaptor<OperationalGatewayRequestPayload> operationalGatewayRequestPayloadArgumentCaptor;

  @EndpointInject(URI_FROM)
  ProducerTemplate from;

  @Override
  protected RoutesBuilder createRouteBuilder() {
    return new InvoiceFinanceResultFlowRouteBuilder(
        invoiceFinancingService,
        corporateLoanService,
        paymentExecutionService,
        operationalGatewayService,
        businessBankingService,
        URI_FROM);
  }

  @Test
  void itShouldSendBothNotificationsToSellerIfSuccessful() {
    FinanceAckMessage invoiceFinanceMessage =
        FinanceAckMessage.builder()
            .invoiceArray(List.of(Invoice.builder().invoiceReference("01-001").build()))
            .buyerIdentifier("B123")
            .sellerIdentifier("S123")
            .financeDealAmount("10000")
            .build();

    when(invoiceFinancingService.findCustomerByMnemonic(anyString()))
        .thenReturn(Customer.builder().build()) // Buyer
        .thenReturn(Customer.builder().fullName("Seller").build()); // Seller
    when(invoiceFinancingService.findProductMasterExtensionByMasterReference(anyString()))
        .thenReturn(ProductMasterExtension.builder().financeAccount("CC0974631820").build());
    when(invoiceFinancingService.findAccountByCustomerMnemonic(anyString()))
        .thenReturn(Account.builder().externalAccountNumber("AH0974631821").build());
    when(invoiceFinancingService.findInvoiceByMasterReference(any()))
        .thenReturn(InvoiceMaster.builder().batchId("b123").build());

    when(corporateLoanService.createCredit(any()))
        .thenReturn(
            new DistributorCreditResponse(
                Data.builder().disbursementAmount(100).error(Error.empty()).build()));
    when(corporateLoanService.simulateCredit(any()))
        .thenReturn(
            new DistributorCreditResponse(
                Data.builder()
                    .disbursementAmount(100)
                    .totalInstallmentsAmount(110)
                    .error(Error.empty())
                    .build()));
    when(invoiceFinancingService.buildInvoiceFinancingEmailInfo(any(), any(), any(), any()))
        .thenReturn(InvoiceEmailInfo.builder().build());
    when(paymentExecutionService.makeTransactionRequest(any()))
        .thenReturn(new BusinessAccountTransfersResponse(new TransferResponseData("OK", "", "")))
        .thenReturn(new BusinessAccountTransfersResponse(new TransferResponseData("OK", "", "")));

    from.sendBody(new AckServiceRequest<>(null, invoiceFinanceMessage));

    verify(invoiceFinancingService, times(2))
        .buildInvoiceFinancingEmailInfo(
            emailEventArgumentCaptor.capture(),
            any(FinanceAckMessage.class),
            customerArgumentCaptor.capture(),
            amountArgumentCaptor.capture());
    verify(operationalGatewayService, times(2))
        .sendNotificationRequest(any(InvoiceEmailInfo.class));

    var customers = customerArgumentCaptor.getAllValues();
    var emailEvents = emailEventArgumentCaptor.getAllValues();
    var amounts = amountArgumentCaptor.getAllValues();

    var expectedAmount = new BigDecimal("100.00");

    assertEquals("Seller", customers.get(0).getFullName());
    assertEquals(InvoiceEmailEvent.FINANCED, emailEvents.get(0));
    assertEquals(expectedAmount, amounts.get(0));

    assertEquals("Seller", customers.get(1).getFullName());
    assertEquals(InvoiceEmailEvent.PROCESSED, emailEvents.get(1));
    assertEquals(expectedAmount, amounts.get(1));
  }

  @Test
  void itShouldSendOneNotificationIfTransactionFailed() {
    FinanceAckMessage invoiceFinanceMessage =
        FinanceAckMessage.builder()
            .invoiceArray(List.of(Invoice.builder().invoiceReference("01-001").build()))
            .buyerIdentifier("B123")
            .sellerIdentifier("S123")
            .financeDealAmount("10000")
            .build();

    when(invoiceFinancingService.findCustomerByMnemonic(anyString()))
        .thenReturn(Customer.builder().build()) // Buyer
        .thenReturn(Customer.builder().build()); // Seller
    when(invoiceFinancingService.findProductMasterExtensionByMasterReference(anyString()))
        .thenReturn(ProductMasterExtension.builder().financeAccount("CC0974631820").build());
    when(invoiceFinancingService.findAccountByCustomerMnemonic(anyString()))
        .thenReturn(Account.builder().externalAccountNumber("AH0974631821").build());
    when(invoiceFinancingService.findInvoiceByMasterReference(any()))
        .thenReturn(InvoiceMaster.builder().batchId("b123").build());

    when(corporateLoanService.createCredit(any()))
        .thenReturn(
            new DistributorCreditResponse(
                Data.builder().disbursementAmount(100).error(Error.empty()).build()));
    when(invoiceFinancingService.buildInvoiceFinancingEmailInfo(any(), any(), any(), any()))
        .thenReturn(InvoiceEmailInfo.builder().build());
    when(paymentExecutionService.makeTransactionRequest(any()))
        .thenReturn(new BusinessAccountTransfersResponse(new TransferResponseData("OK", "", "")))
        .thenReturn(
            new BusinessAccountTransfersResponse(new TransferResponseData("FAILED", "", "")));

    from.sendBody(new AckServiceRequest<>(null, invoiceFinanceMessage));

    verify(operationalGatewayService, times(1))
        .sendNotificationRequest(any(InvoiceEmailInfo.class));
  }

  @Test
  void itShouldSendNotifyOperationalGwIfCreditFailed() {
    FinanceAckMessage invoiceFinanceMessage =
        FinanceAckMessage.builder()
            .invoiceArray(List.of(Invoice.builder().invoiceReference("01-001").build()))
            .buyerIdentifier("B123")
            .sellerIdentifier("S123")
            .financeDealAmount("10000")
            .build();

    when(invoiceFinancingService.findCustomerByMnemonic(anyString()))
        .thenReturn(Customer.builder().build()) // Buyer
        .thenReturn(Customer.builder().build()); // Seller
    when(invoiceFinancingService.findProductMasterExtensionByMasterReference(anyString()))
        .thenReturn(ProductMasterExtension.builder().financeAccount("CC0974631820").build());
    when(invoiceFinancingService.findAccountByCustomerMnemonic(anyString()))
        .thenReturn(Account.builder().externalAccountNumber("AH0974631821").build());
    when(invoiceFinancingService.findInvoiceByMasterReference(any()))
        .thenReturn(InvoiceMaster.builder().batchId("b123").build());

    when(corporateLoanService.createCredit(any()))
        .thenThrow(new CreditCreationException("Error"))
        .thenReturn(
            new DistributorCreditResponse(
                Data.builder().error(new Error("001", "Error", "ERROR")).build()));

    from.sendBody(new AckServiceRequest<>(null, invoiceFinanceMessage));
    from.sendBody(new AckServiceRequest<>(null, invoiceFinanceMessage));

    verify(operationalGatewayService, times(2)).sendNotificationRequest(any());
    verify(businessBankingService, times(2)).notifyEvent(any(), any());
  }

  @Test
  void itShouldSendNotificationIfCreditSimulationFails() {
    FinanceAckMessage invoiceFinanceMessage =
        FinanceAckMessage.builder()
            .invoiceArray(List.of(Invoice.builder().invoiceReference("01-001").build()))
            .buyerIdentifier("B123")
            .sellerIdentifier("S123")
            .financeDealAmount("10000")
            .build();

    when(invoiceFinancingService.findCustomerByMnemonic(anyString()))
        .thenReturn(Customer.builder().build()) // Buyer
        .thenReturn(Customer.builder().fullName("Seller").build()); // Seller
    when(invoiceFinancingService.findProductMasterExtensionByMasterReference(anyString()))
        .thenReturn(ProductMasterExtension.builder().financeAccount("CC0974631820").build());
    when(invoiceFinancingService.findAccountByCustomerMnemonic(anyString()))
        .thenReturn(Account.builder().externalAccountNumber("AH0974631821").build());
    when(invoiceFinancingService.findInvoiceByMasterReference(any()))
        .thenReturn(InvoiceMaster.builder().batchId("b123").build());

    when(corporateLoanService.createCredit(any()))
        .thenReturn(
            new DistributorCreditResponse(
                Data.builder().error(new Error("01", "Something went wrong", "")).build()));
    when(invoiceFinancingService.buildInvoiceFinancingEmailInfo(any(), any(), any(), any()))
        .thenReturn(InvoiceEmailInfo.builder().build());

    from.sendBody(new AckServiceRequest<>(null, invoiceFinanceMessage));

    verify(invoiceFinancingService)
        .buildInvoiceFinancingEmailInfo(
            emailEventArgumentCaptor.capture(),
            any(FinanceAckMessage.class),
            customerArgumentCaptor.capture(),
            amountArgumentCaptor.capture());
    verify(operationalGatewayService).sendNotificationRequest(any(InvoiceEmailInfo.class));
    verify(businessBankingService)
        .notifyEvent(
            operationalGatewayProcessCodeArgumentCaptor.capture(),
            operationalGatewayRequestPayloadArgumentCaptor.capture());

    var customers = customerArgumentCaptor.getAllValues();
    var emailEvents = emailEventArgumentCaptor.getAllValues();
    var amounts = amountArgumentCaptor.getAllValues();

    var expectedAmount = new BigDecimal("100.00");

    assertEquals("Seller", customers.get(0).getFullName());
    assertEquals(InvoiceEmailEvent.FINANCED, emailEvents.get(0));
    assertEquals(expectedAmount, amounts.get(0));

    assertEquals(
        PayloadStatus.FAILED.getValue(),
        operationalGatewayRequestPayloadArgumentCaptor.getValue().status());
  }

  @Test
  void itShouldSendNotificationIfTransactionFromSellerToBuyerFails() {
    FinanceAckMessage invoiceFinanceMessage =
        FinanceAckMessage.builder()
            .invoiceArray(List.of(Invoice.builder().invoiceReference("01-001").build()))
            .buyerIdentifier("B123")
            .sellerIdentifier("S123")
            .financeDealAmount("10000")
            .build();

    when(invoiceFinancingService.findCustomerByMnemonic(anyString()))
        .thenReturn(Customer.builder().build()) // Buyer
        .thenReturn(Customer.builder().fullName("Seller").build()); // Seller
    when(invoiceFinancingService.findProductMasterExtensionByMasterReference(anyString()))
        .thenReturn(ProductMasterExtension.builder().financeAccount("CC0974631820").build());
    when(invoiceFinancingService.findAccountByCustomerMnemonic(anyString()))
        .thenReturn(Account.builder().externalAccountNumber("AH0974631821").build());
    when(invoiceFinancingService.findInvoiceByMasterReference(any()))
        .thenReturn(InvoiceMaster.builder().batchId("b123").build());

    when(corporateLoanService.createCredit(any()))
        .thenReturn(
            new DistributorCreditResponse(
                Data.builder().disbursementAmount(100).error(Error.empty()).build()));
    when(corporateLoanService.simulateCredit(any()))
        .thenReturn(
            new DistributorCreditResponse(
                Data.builder()
                    .disbursementAmount(100)
                    .totalInstallmentsAmount(110)
                    .error(Error.empty())
                    .build()));
    when(invoiceFinancingService.buildInvoiceFinancingEmailInfo(any(), any(), any(), any()))
        .thenReturn(InvoiceEmailInfo.builder().build());
    when(paymentExecutionService.makeTransactionRequest(any()))
        // Buyer to Seller Transaction (Invoice amount)
        .thenReturn(new BusinessAccountTransfersResponse(new TransferResponseData("OK", "", "")))
        .thenReturn(new BusinessAccountTransfersResponse(new TransferResponseData("OK", "", "")))
        // Seller to Buyer (solca and taxes)
        .thenReturn(
            new BusinessAccountTransfersResponse(new TransferResponseData("FAILED", "", "")));

    from.sendBody(new AckServiceRequest<>(null, invoiceFinanceMessage));

    verify(invoiceFinancingService)
        .buildInvoiceFinancingEmailInfo(
            emailEventArgumentCaptor.capture(),
            any(FinanceAckMessage.class),
            customerArgumentCaptor.capture(),
            amountArgumentCaptor.capture());
    verify(operationalGatewayService).sendNotificationRequest(any(InvoiceEmailInfo.class));
    verify(businessBankingService)
        .notifyEvent(
            operationalGatewayProcessCodeArgumentCaptor.capture(),
            operationalGatewayRequestPayloadArgumentCaptor.capture());

    var expectedAmount = new BigDecimal("100.00");
    assertEquals("Seller", customerArgumentCaptor.getValue().getFullName());
    assertEquals(InvoiceEmailEvent.FINANCED, emailEventArgumentCaptor.getValue());
    assertEquals(expectedAmount, amountArgumentCaptor.getValue());

    assertEquals(
        PayloadStatus.FAILED.getValue(),
        operationalGatewayRequestPayloadArgumentCaptor.getValue().status());
  }
}
