package com.tcmp.tiapi.invoice.service;

import com.tcmp.tiapi.invoice.model.InvoiceEventInfo;
import com.tcmp.tiapi.invoice.repository.redis.InvoiceCreationEventRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvoiceEventServiceTest {
  @Mock private InvoiceCreationEventRepository invoiceCreationEventRepository;

  private InvoiceEventService testedInvoiceEventService;

  @BeforeEach
  void setUp() {
    testedInvoiceEventService = new InvoiceEventService(invoiceCreationEventRepository);
  }

  @Test
  void findInvoiceEventInfoByUuid_itShouldReturnInvoiceEventInfo() {
    String invoiceInfoUuid = "1-1-1-1";

    when(invoiceCreationEventRepository.findById(anyString()))
      .thenReturn(Optional.of(InvoiceEventInfo.builder().build()));

    assertNotNull(testedInvoiceEventService.findInvoiceEventInfoByUuid(invoiceInfoUuid));
  }

  @Test
  void findInvoiceEventInfoByUuid_itShouldThrowException() {
    when(invoiceCreationEventRepository.findById(anyString()))
      .thenReturn(Optional.empty());

    assertThrows(
      EntityNotFoundException.class,
      () -> testedInvoiceEventService.findInvoiceEventInfoByUuid("")
    );
  }
}
