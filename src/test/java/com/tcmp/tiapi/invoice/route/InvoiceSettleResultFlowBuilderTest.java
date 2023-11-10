package com.tcmp.tiapi.invoice.route;

import com.tcmp.tiapi.customer.model.Account;
import com.tcmp.tiapi.customer.model.Customer;
import com.tcmp.tiapi.invoice.dto.ti.CreateDueInvoiceEventMessage;
import com.tcmp.tiapi.invoice.model.InvoiceMaster;
import com.tcmp.tiapi.invoice.model.ProductMasterExtension;
import com.tcmp.tiapi.invoice.service.InvoiceSettlementService;
import com.tcmp.tiapi.messaging.model.requests.AckServiceRequest;
import com.tcmp.tiapi.program.model.ProgramExtension;
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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceSettleResultFlowBuilderTest extends CamelTestSupport {
  private static final String URI_FROM = "direct:startInvoiceSettleFlow";

  @Mock private InvoiceSettlementService invoiceSettlementService;
  @Mock private CorporateLoanService corporateLoanService;
  @Mock private PaymentExecutionService paymentExecutionService;
  @Mock private OperationalGatewayService operationalGatewayService;

  @Captor ArgumentCaptor<InvoiceEmailEvent> invoiceEmailEventArgumentCaptor;
  @Captor ArgumentCaptor<Customer> customerArgumentCaptor;

  @EndpointInject(URI_FROM) ProducerTemplate from;


  @Override
  protected RoutesBuilder createRouteBuilder() {
    return new InvoiceSettleResultFlowBuilder(
      invoiceSettlementService,
      corporateLoanService,
      paymentExecutionService,
      operationalGatewayService,

      URI_FROM
    );
  }

  @Test
  void itShouldSendNotificationToBuyerIfExtraFinancedAndHasBeenFinanced() {
    Customer buyer = Customer.builder().fullName("Buyer").build();
    Customer seller = Customer.builder().fullName("Seller").build();

    when(invoiceSettlementService.findCustomerByMnemonic(anyString()))
      .thenReturn(buyer)
      .thenReturn(seller);
    when(invoiceSettlementService.findProgrammeExtensionByIdOrDefault(anyString()))
      .thenReturn(ProgramExtension.builder().extraFinancingDays(30).build());
    when(invoiceSettlementService.findInvoiceByMasterRef(anyString()))
      .thenReturn(InvoiceMaster.builder().id(1L).build());
    when(invoiceSettlementService.invoiceHasLinkedFinanceEvent(any()))
      .thenReturn(true);
    when(invoiceSettlementService.buildInvoiceSettlementEmailInfo(any(), any(), any(), any()))
      .thenReturn(InvoiceEmailInfo.builder().build());
    when(invoiceSettlementService.findProductMasterExtensionByMasterId(anyLong()))
      .thenReturn(ProductMasterExtension.builder()
        .financeAccount("AH9278281280")
        .build());

    from.sendBody(new AckServiceRequest<>(null, buildMockSettlementAck()));

    verify(invoiceSettlementService).buildInvoiceSettlementEmailInfo(
      any(),
      customerArgumentCaptor.capture(),
      invoiceEmailEventArgumentCaptor.capture(),
      any()
    );
    verify(operationalGatewayService).sendNotificationRequest(any(InvoiceEmailInfo.class));
    assertEquals(buyer, customerArgumentCaptor.getValue());
    assertEquals(InvoiceEmailEvent.CREDITED, invoiceEmailEventArgumentCaptor.getValue());
  }

  @Test
  void itShouldSendBuyerNotificationAndSellerNotificationsIfExtraFinancedAndHasNotBeenFinanced() {
    Customer buyer = Customer.builder().fullName("Buyer").build();
    Customer seller = Customer.builder().fullName("Seller").build();

    when(invoiceSettlementService.findCustomerByMnemonic(anyString()))
      .thenReturn(buyer)
      .thenReturn(seller);
    when(invoiceSettlementService.findProgrammeExtensionByIdOrDefault(anyString()))
      .thenReturn(ProgramExtension.builder().extraFinancingDays(30).build());
    when(invoiceSettlementService.findInvoiceByMasterRef(anyString()))
      .thenReturn(InvoiceMaster.builder().id(1L).build());
    when(invoiceSettlementService.invoiceHasLinkedFinanceEvent(any()))
      .thenReturn(false);
    when(invoiceSettlementService.buildInvoiceSettlementEmailInfo(any(), any(), any(), any()))
      .thenReturn(InvoiceEmailInfo.builder().build());
    when(invoiceSettlementService.findProductMasterExtensionByMasterId(anyLong()))
      .thenReturn(ProductMasterExtension.builder()
        .financeAccount("AH9278281280")
        .build());
    when(invoiceSettlementService.findAccountByCustomerMnemonic(anyString()))
      .thenReturn(Account.builder()
        .externalAccountNumber("AH9278281281")
        .build());
    when(corporateLoanService.createCredit(any()))
      .thenReturn(new DistributorCreditResponse(Data.builder()
        .error(new Error("", "", "INFO"))
        .build()));
    when(paymentExecutionService.makeTransactionRequest(any()))
      .thenReturn(new BusinessAccountTransfersResponse(TransferResponseData.builder()
        .status("OK")
        .build()
      ));

    from.sendBody(new AckServiceRequest<>(null, buildMockSettlementAck()));

    verify(corporateLoanService).createCredit(any());
    verify(paymentExecutionService).makeTransactionRequest(any());

    verify(invoiceSettlementService, times(3)).buildInvoiceSettlementEmailInfo(
      any(), customerArgumentCaptor.capture(), invoiceEmailEventArgumentCaptor.capture(), any(BigDecimal.class));
    verify(operationalGatewayService, times(3)).sendNotificationRequest(any(InvoiceEmailInfo.class));

    var firstCustomer = customerArgumentCaptor.getAllValues().get(0);
    var secondCustomer = customerArgumentCaptor.getAllValues().get(1);
    var thirdCustomer = customerArgumentCaptor.getAllValues().get(2);

    var firstEmailEvent = invoiceEmailEventArgumentCaptor.getAllValues().get(0);
    var secondEmailEvent = invoiceEmailEventArgumentCaptor.getAllValues().get(1);
    var thirdEmailEvent = invoiceEmailEventArgumentCaptor.getAllValues().get(2);

    assertEquals(seller.getFullName(), firstCustomer.getFullName());
    assertEquals(InvoiceEmailEvent.SETTLED, firstEmailEvent);

    assertEquals(buyer.getFullName(), secondCustomer.getFullName());
    assertEquals(InvoiceEmailEvent.CREDITED, secondEmailEvent);

    assertEquals(seller.getFullName(), thirdCustomer.getFullName());
    assertEquals(InvoiceEmailEvent.PROCESSED, thirdEmailEvent);
  }

  private CreateDueInvoiceEventMessage buildMockSettlementAck() {
    return CreateDueInvoiceEventMessage.builder()
      .buyerIdentifier("B123")
      .sellerIdentifier("")
      .programme("P123")
      .masterRef("M123")
      .paymentAmount("10")
      .build();
  }
}
