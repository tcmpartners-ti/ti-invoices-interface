package com.tcmp.tiapi.invoice.route;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.tcmp.tiapi.customer.model.Account;
import com.tcmp.tiapi.customer.model.Customer;
import com.tcmp.tiapi.invoice.dto.ti.CreateDueInvoiceEventMessage;
import com.tcmp.tiapi.invoice.model.InvoiceMaster;
import com.tcmp.tiapi.invoice.model.ProductMasterExtension;
import com.tcmp.tiapi.invoice.service.InvoiceSettlementService;
import com.tcmp.tiapi.messaging.model.requests.AckServiceRequest;
import com.tcmp.tiapi.program.model.ProgramExtension;
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
import com.tcmp.tiapi.titoapigee.paymentexecution.dto.response.TransferResponseError;
import com.tcmp.tiapi.titoapigee.paymentexecution.exception.PaymentExecutionException;
import java.math.BigDecimal;
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
class InvoiceSettleResultFlowBuilderTest extends CamelTestSupport {
  private static final String URI_FROM = "direct:startInvoiceSettleFlow";

  @Mock private InvoiceSettlementService invoiceSettlementService;
  @Mock private CorporateLoanService corporateLoanService;
  @Mock private PaymentExecutionService paymentExecutionService;
  @Mock private OperationalGatewayService operationalGatewayService;
  @Mock private BusinessBankingService businessBankingService;

  @Captor ArgumentCaptor<InvoiceEmailEvent> invoiceEmailEventArgumentCaptor;
  @Captor ArgumentCaptor<Customer> customerArgumentCaptor;
  @Captor ArgumentCaptor<OperationalGatewayRequestPayload> operationalGatewayRequestPayloadArgumentCaptor;

  @EndpointInject(URI_FROM) ProducerTemplate from;

  @Override
  protected RoutesBuilder createRouteBuilder() {
    return new InvoiceSettleResultFlowBuilder(
      invoiceSettlementService,
      corporateLoanService,
      paymentExecutionService,
      operationalGatewayService,
      businessBankingService,

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
      .thenReturn(InvoiceMaster.builder().id(1L).batchId("b123").build());
    when(invoiceSettlementService.invoiceHasLinkedFinanceEvent(any()))
      .thenReturn(true);
    when(invoiceSettlementService.buildInvoiceSettlementEmailInfo(any(), any(), any(), any()))
      .thenReturn(InvoiceEmailInfo.builder().build());
    when(invoiceSettlementService.findProductMasterExtensionByMasterId(anyLong()))
      .thenReturn(ProductMasterExtension.builder().financeAccount("AH9278281280").build());

    from.sendBody(new AckServiceRequest<>(null, buildMockSettlementAck()));

    verify(invoiceSettlementService).buildInvoiceSettlementEmailInfo(
      invoiceEmailEventArgumentCaptor.capture(),
      any(),
      customerArgumentCaptor.capture(),
      any()
    );
    verify(operationalGatewayService).sendNotificationRequest(any(InvoiceEmailInfo.class));
    assertEquals(buyer, customerArgumentCaptor.getValue());
    assertEquals(InvoiceEmailEvent.CREDITED, invoiceEmailEventArgumentCaptor.getValue());
  }

  @Test
  void itShouldNotifyIfCreditCreationFails() {
    Customer buyer = Customer.builder().fullName("Buyer").build();
    Customer seller = Customer.builder().fullName("Seller").build();

    when(invoiceSettlementService.findCustomerByMnemonic(anyString()))
      .thenReturn(buyer)
      .thenReturn(seller);
    when(invoiceSettlementService.findProgrammeExtensionByIdOrDefault(anyString()))
      .thenReturn(ProgramExtension.builder().extraFinancingDays(30).build());
    when(invoiceSettlementService.findInvoiceByMasterRef(anyString()))
      .thenReturn(InvoiceMaster.builder().id(1L).batchId("b123").build());
    when(invoiceSettlementService.invoiceHasLinkedFinanceEvent(any()))
      .thenReturn(false);
    when(invoiceSettlementService.buildInvoiceSettlementEmailInfo(any(), any(), any(), any()))
      .thenReturn(InvoiceEmailInfo.builder().build());
    when(invoiceSettlementService.findProductMasterExtensionByMasterId(anyLong()))
      .thenReturn(ProductMasterExtension.builder().financeAccount("AH9278281280").build());

    // Two types of errors.
    when(corporateLoanService.createCredit(any()))
      .thenReturn(new DistributorCreditResponse(Data.builder()
        .error(new Error("123", "Invalid credit.", "ERROR"))
        .build()))
      .thenThrow(new CreditCreationException("Error"));

    from.sendBody(new AckServiceRequest<>(null, buildMockSettlementAck()));
    from.sendBody(new AckServiceRequest<>(null, buildMockSettlementAck()));

    verify(corporateLoanService, times(2)).createCredit(any());
    verify(businessBankingService, times(2)).notifyEvent(
      eq(OperationalGatewayProcessCode.INVOICE_SETTLEMENT),
      operationalGatewayRequestPayloadArgumentCaptor.capture()
    );

    var payloads = operationalGatewayRequestPayloadArgumentCaptor.getAllValues();
    assertEquals(PayloadStatus.FAILED.getValue(), payloads.get(0).status());
    assertEquals(PayloadStatus.FAILED.getValue(), payloads.get(1).status());
  }


  @Test
  void itShouldNotifyIfTransactionFails() {
    Customer buyer = Customer.builder().fullName("Buyer").build();
    Customer seller = Customer.builder().fullName("Seller").build();

    when(invoiceSettlementService.findCustomerByMnemonic(anyString()))
      .thenReturn(buyer)
      .thenReturn(seller);
    when(invoiceSettlementService.findProgrammeExtensionByIdOrDefault(anyString()))
      .thenReturn(ProgramExtension.builder().extraFinancingDays(30).build());
    when(invoiceSettlementService.findInvoiceByMasterRef(anyString()))
      .thenReturn(InvoiceMaster.builder().id(1L).batchId("b123").build());
    when(invoiceSettlementService.invoiceHasLinkedFinanceEvent(any()))
      .thenReturn(false);
    when(invoiceSettlementService.buildInvoiceSettlementEmailInfo(any(), any(), any(), any()))
      .thenReturn(InvoiceEmailInfo.builder().build());
    when(invoiceSettlementService.findProductMasterExtensionByMasterId(anyLong()))
      .thenReturn(ProductMasterExtension.builder().financeAccount("AH9278281280").build());
    when(invoiceSettlementService.findAccountByCustomerMnemonic(anyString()))
      .thenReturn(Account.builder().externalAccountNumber("AH9278281281").build());

    when(corporateLoanService.createCredit(any()))
      .thenReturn(new DistributorCreditResponse(Data.builder()
        .error(new Error("", "", "INFO"))
        .build()));

    // Two Types of errors
    when(paymentExecutionService.makeTransactionRequest(any()))
      // First Case, one of the two transaction fails
      .thenReturn(new BusinessAccountTransfersResponse(TransferResponseData.builder().status("OK").build()))
      .thenReturn(new BusinessAccountTransfersResponse(TransferResponseData.builder().status("NOT_OK").build()))
      // Second case, request error
      .thenThrow(new PaymentExecutionException(TransferResponseError.builder().build()));

    from.sendBody(new AckServiceRequest<>(null, buildMockSettlementAck()));
    from.sendBody(new AckServiceRequest<>(null, buildMockSettlementAck()));

    verify(corporateLoanService, times(2)).createCredit(any());
    verify(businessBankingService, times(2)).notifyEvent(
      eq(OperationalGatewayProcessCode.INVOICE_SETTLEMENT),
      operationalGatewayRequestPayloadArgumentCaptor.capture()
    );

    var payloads = operationalGatewayRequestPayloadArgumentCaptor.getAllValues();
    assertEquals(PayloadStatus.FAILED.getValue(), payloads.get(0).status());
    assertEquals(PayloadStatus.FAILED.getValue(), payloads.get(1).status());
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
      .thenReturn(InvoiceMaster.builder().id(1L).batchId("b123").build());
    when(invoiceSettlementService.invoiceHasLinkedFinanceEvent(any()))
      .thenReturn(false);
    when(invoiceSettlementService.buildInvoiceSettlementEmailInfo(any(), any(), any(), any()))
      .thenReturn(InvoiceEmailInfo.builder().build());
    when(invoiceSettlementService.findProductMasterExtensionByMasterId(anyLong()))
      .thenReturn(ProductMasterExtension.builder().financeAccount("AH9278281280").build());
    when(invoiceSettlementService.findAccountByCustomerMnemonic(anyString()))
      .thenReturn(Account.builder().externalAccountNumber("AH9278281281").build());

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
    verify(paymentExecutionService, times(2)).makeTransactionRequest(any());

    verify(invoiceSettlementService, times(3)).buildInvoiceSettlementEmailInfo(
      invoiceEmailEventArgumentCaptor.capture(), any(), customerArgumentCaptor.capture(), any(BigDecimal.class));
    verify(operationalGatewayService, times(3)).sendNotificationRequest(any(InvoiceEmailInfo.class));
    verify(businessBankingService).notifyEvent(eq(OperationalGatewayProcessCode.INVOICE_SETTLEMENT), any());

    var customers = customerArgumentCaptor.getAllValues();
    var emailEvents = invoiceEmailEventArgumentCaptor.getAllValues();

    assertEquals(seller.getFullName(), customers.get(0).getFullName());
    assertEquals(InvoiceEmailEvent.SETTLED, emailEvents.get(0));

    assertEquals(buyer.getFullName(), customers.get(1).getFullName());
    assertEquals(InvoiceEmailEvent.CREDITED, emailEvents.get(1));

    assertEquals(seller.getFullName(), customers.get(2).getFullName());
    assertEquals(InvoiceEmailEvent.PROCESSED, emailEvents.get(2));
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
