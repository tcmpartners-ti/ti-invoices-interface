package com.tcmp.tiapi.invoice.route;

import com.tcmp.tiapi.customer.model.Account;
import com.tcmp.tiapi.customer.model.Customer;
import com.tcmp.tiapi.invoice.dto.ti.financeack.FinanceAckMessage;
import com.tcmp.tiapi.invoice.dto.ti.financeack.Invoice;
import com.tcmp.tiapi.invoice.model.InvoiceMaster;
import com.tcmp.tiapi.invoice.model.ProductMasterExtension;
import com.tcmp.tiapi.invoice.service.InvoiceFinancingService;
import com.tcmp.tiapi.messaging.model.requests.AckServiceRequest;
import com.tcmp.tiapi.titoapigee.businessbanking.BusinessBankingService;
import com.tcmp.tiapi.titoapigee.corporateloan.CorporateLoanService;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.response.Data;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.response.DistributorCreditResponse;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.response.Error;
import com.tcmp.tiapi.titoapigee.operationalgateway.OperationalGatewayService;
import com.tcmp.tiapi.titoapigee.operationalgateway.model.InvoiceEmailEvent;
import com.tcmp.tiapi.titoapigee.operationalgateway.model.InvoiceEmailInfo;
import com.tcmp.tiapi.titoapigee.paymentexecution.PaymentExecutionService;
import com.tcmp.tiapi.titoapigee.paymentexecution.dto.response.BusinessAccountTransfersResponse;
import com.tcmp.tiapi.titoapigee.paymentexecution.dto.response.TransferResponseData;
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

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

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

  @EndpointInject(URI_FROM) ProducerTemplate from;

  @Override
  protected RoutesBuilder createRouteBuilder() {
    return new InvoiceFinanceResultFlowRouteBuilder(
      invoiceFinancingService,
      corporateLoanService,
      paymentExecutionService,
      operationalGatewayService,
      businessBankingService,

      URI_FROM
    );
  }

  @Test
  void itShouldSendBothNotificationsToSellerIfSuccessful() {
    FinanceAckMessage invoiceFinanceMessage = FinanceAckMessage.builder()
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
      .thenReturn(new DistributorCreditResponse(Data.builder()
        .disbursementAmount(100)
        .error(Error.empty())
        .build()));
    when(invoiceFinancingService.buildInvoiceFinancingEmailInfo(any(), any(), any(), any()))
      .thenReturn(InvoiceEmailInfo.builder().build());
    when(paymentExecutionService.makeTransactionRequest(any()))
      .thenReturn(new BusinessAccountTransfersResponse(
        new TransferResponseData("OK", "", "")))
      .thenReturn(new BusinessAccountTransfersResponse(
        new TransferResponseData("OK", "", "")));

    from.sendBody(new AckServiceRequest<>(null, invoiceFinanceMessage));

    verify(invoiceFinancingService, times(2)).buildInvoiceFinancingEmailInfo(
      emailEventArgumentCaptor.capture(),
      any(FinanceAckMessage.class),
      customerArgumentCaptor.capture(),
      amountArgumentCaptor.capture()
    );
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
    FinanceAckMessage invoiceFinanceMessage = FinanceAckMessage.builder()
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
      .thenReturn(new DistributorCreditResponse(Data.builder()
        .disbursementAmount(100)
        .error(Error.empty())
        .build()));
    when(invoiceFinancingService.buildInvoiceFinancingEmailInfo(any(), any(), any(), any()))
      .thenReturn(InvoiceEmailInfo.builder().build());
    when(paymentExecutionService.makeTransactionRequest(any()))
      .thenReturn(new BusinessAccountTransfersResponse(
        new TransferResponseData("OK", "", "")))
      .thenReturn(new BusinessAccountTransfersResponse(
        new TransferResponseData("FAILED", "", "")));

    from.sendBody(new AckServiceRequest<>(null, invoiceFinanceMessage));

    verify(operationalGatewayService, times(1))
      .sendNotificationRequest(any(InvoiceEmailInfo.class));
  }
}
