package com.tcmp.tiapi.invoice.strategy.ticc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.tcmp.tiapi.customer.model.Address;
import com.tcmp.tiapi.customer.model.Customer;
import com.tcmp.tiapi.customer.repository.CustomerRepository;
import com.tcmp.tiapi.invoice.dto.ti.creation.InvoiceCreationResultMessage;
import com.tcmp.tiapi.ti.dto.request.AckServiceRequest;
import com.tcmp.tiapi.titoapigee.operationalgateway.OperationalGatewayService;
import com.tcmp.tiapi.titoapigee.operationalgateway.model.InvoiceEmailInfo;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InvoiceCreationResultFlowStrategyTest {
  @Mock private OperationalGatewayService operationalGatewayService;
  @Mock private CustomerRepository customerRepository;

  @Captor private ArgumentCaptor<InvoiceEmailInfo> invoiceEmailInfoArgumentCaptor;

  private InvoiceCreationResultFlowStrategy invoiceCreationResultFlowStrategy;

  @BeforeEach
  void setUp() {
    invoiceCreationResultFlowStrategy =
        new InvoiceCreationResultFlowStrategy(operationalGatewayService, customerRepository);
  }

  @Test
  void handleServiceRequest_itShouldNotifyInvoiceCreation() {
    var message = buildMockMessage();

    when(customerRepository.findFirstByIdMnemonic(anyString()))
        .thenReturn(
            Optional.of(
                Customer.builder()
                    .address(Address.builder().customerEmail("jjoestar@d4c.com      ").build())
                    .fullName("Johnny Joestar      ")
                    .build()));

    invoiceCreationResultFlowStrategy.handleServiceRequest(new AckServiceRequest<>(null, message));

    verify(customerRepository).findFirstByIdMnemonic("1722466421");
    verify(operationalGatewayService)
        .sendNotificationRequest(invoiceEmailInfoArgumentCaptor.capture());

    assertNotNull(invoiceEmailInfoArgumentCaptor.getValue());
    assertEquals("1722466421", invoiceEmailInfoArgumentCaptor.getValue().customerMnemonic());
    assertEquals("2023-12-15", invoiceEmailInfoArgumentCaptor.getValue().date());
    assertEquals("jjoestar@d4c.com", invoiceEmailInfoArgumentCaptor.getValue().customerEmail());
    assertEquals("Johnny Joestar", invoiceEmailInfoArgumentCaptor.getValue().customerName());
  }

  private InvoiceCreationResultMessage buildMockMessage() {
    return InvoiceCreationResultMessage.builder()
        .sellerIdentifier("1722466421")
        .receivedOn("2023-12-15")
        .invoiceNumber("001-002-000000782")
        .faceValueCurrency("USD")
        .faceValueAmount("60.45")
        .build();
  }
}
