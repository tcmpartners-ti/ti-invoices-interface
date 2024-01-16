package com.tcmp.tiapi.invoice.strategy.ticc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.tcmp.tiapi.customer.model.Address;
import com.tcmp.tiapi.customer.model.Customer;
import com.tcmp.tiapi.customer.repository.CustomerRepository;
import com.tcmp.tiapi.invoice.dto.ti.cancel.CancelInvoiceEventMessage;
import com.tcmp.tiapi.invoice.model.InvoiceMaster;
import com.tcmp.tiapi.invoice.repository.InvoiceRepository;
import com.tcmp.tiapi.ti.dto.request.AckServiceRequest;
import com.tcmp.tiapi.titoapigee.operationalgateway.OperationalGatewayService;
import com.tcmp.tiapi.titoapigee.operationalgateway.model.InvoiceEmailInfo;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InvoiceCancellationFlowStrategyTest {
  @Mock private OperationalGatewayService operationalGatewayService;

  @Mock private InvoiceRepository invoiceRepository;
  @Mock private CustomerRepository customerRepository;

  @Captor private ArgumentCaptor<InvoiceEmailInfo> invoiceEmailInfoArgumentCaptor;

  @InjectMocks private InvoiceCancellationFlowStrategy invoiceCancellationFlowStrategy;

  @Test
  void handleServiceRequest_itShouldHandlePossibleExceptions() {
    when(customerRepository.findFirstByIdMnemonic(anyString()))
        .thenReturn(
            Optional.of(
                Customer.builder()
                    .address(Address.builder().customerEmail("seller@mail.com").build())
                    .fullName("Seller Bucciarati")
                    .build()))
        .thenReturn(Optional.empty());
    when(invoiceRepository.findByProductMasterMasterReference(anyString()))
        .thenReturn(Optional.empty());

    invoiceCancellationFlowStrategy.handleServiceRequest(
        new AckServiceRequest<>(null, buildMockMessage()));
    invoiceCancellationFlowStrategy.handleServiceRequest(
        new AckServiceRequest<>(null, buildMockMessage()));

    verifyNoInteractions(operationalGatewayService);
  }

  @Test
  void handleServiceRequest_itShouldNotifyInvoiceCancellation() {
    var message = buildMockMessage();

    when(customerRepository.findFirstByIdMnemonic(anyString()))
        .thenReturn(
            Optional.of(
                Customer.builder()
                    .address(Address.builder().customerEmail("jjoestar@dann.com").build())
                    .fullName("Joseph Joestar       ")
                    .build()));
    when(invoiceRepository.findByProductMasterMasterReference(anyString()))
        .thenReturn(
            Optional.of(
                InvoiceMaster.builder()
                    .faceValueAmount(BigDecimal.valueOf(1010))
                    .faceValueCurrencyCode("USD              ")
                    .build()));

    invoiceCancellationFlowStrategy.handleServiceRequest(new AckServiceRequest<>(null, message));

    verify(customerRepository).findFirstByIdMnemonic(anyString());
    verify(invoiceRepository).findByProductMasterMasterReference(anyString());
    verify(operationalGatewayService)
        .sendNotificationRequest(invoiceEmailInfoArgumentCaptor.capture());

    assertEquals("1722466421", invoiceEmailInfoArgumentCaptor.getValue().customerMnemonic());
    assertEquals("jjoestar@dann.com", invoiceEmailInfoArgumentCaptor.getValue().customerEmail());
    assertEquals("Joseph Joestar", invoiceEmailInfoArgumentCaptor.getValue().customerName());
    assertEquals(new BigDecimal("10.10"), invoiceEmailInfoArgumentCaptor.getValue().amount());
    assertEquals("USD", invoiceEmailInfoArgumentCaptor.getValue().invoiceCurrency());
  }

  private CancelInvoiceEventMessage buildMockMessage() {
    return CancelInvoiceEventMessage.builder()
        .sellerIdentifier("1722466421")
        .cancellationDate("2023-12-18")
        .invoiceNumber("001-002-000000782")
        .masterRef("12345")
        .build();
  }
}
