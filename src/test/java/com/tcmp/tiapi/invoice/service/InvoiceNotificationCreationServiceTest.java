package com.tcmp.tiapi.invoice.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.tcmp.tiapi.customer.model.Address;
import com.tcmp.tiapi.customer.model.Customer;
import com.tcmp.tiapi.customer.repository.CustomerRepository;
import com.tcmp.tiapi.invoice.dto.ti.creation.InvoiceCreationResultMessage;
import com.tcmp.tiapi.titoapigee.operationalgateway.model.InvoiceEmailEvent;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InvoiceNotificationCreationServiceTest {

  @Mock private CustomerRepository customerRepository;

  private InvoiceNotificationCreationService invoiceNotificationCreationService;

  @BeforeEach
  void setUp() {
    invoiceNotificationCreationService = new InvoiceNotificationCreationService(
      customerRepository
    );
  }

  @Test
  void findCustomerByMnemonic_itShouldThrowExceptionWhenNotFound() {
    when(customerRepository.findFirstByIdMnemonic(anyString()))
      .thenReturn(Optional.empty());

    assertThrows(
      EntityNotFoundException.class,
      () -> invoiceNotificationCreationService.findCustomerByMnemonic("")
    );
  }

  @Test
  void findCustomerByMnemonic_itShouldReturnCustomer() {
    when(customerRepository.findFirstByIdMnemonic(anyString()))
      .thenReturn(Optional.of(Customer.builder().build()));

    var actualCustomer = invoiceNotificationCreationService.findCustomerByMnemonic("");

    assertNotNull(actualCustomer);
  }

  @Test
  void buildInvoiceNotificationCreationEmailInfo_itShouldBuildRequest() {
    var notificationInvoiceCreation = InvoiceCreationResultMessage.builder()
      .sellerIdentifier("1722466420001")
      .faceValueCurrency("USD")
      .invoiceNumber("001-001-0001")
      .faceValueAmount("100")
      .build();

    var customer = Customer.builder()
      .fullName("David Reyes         ")
      .address(Address.builder()
        .customerEmail("dareyesp@pichincha.com")
        .build())
      .build();

    var actualRequest = invoiceNotificationCreationService.buildInvoiceNotificationCreationEmailInfo(
      notificationInvoiceCreation, customer, InvoiceEmailEvent.POSTED);

    String expectedName = "David Reyes";
    assertNotNull(actualRequest);
    assertEquals(customer.getAddress().getCustomerEmail(), actualRequest.customerEmail());
    assertEquals(expectedName, actualRequest.customerName());
    assertNotNull(actualRequest.amount());
  }
}
