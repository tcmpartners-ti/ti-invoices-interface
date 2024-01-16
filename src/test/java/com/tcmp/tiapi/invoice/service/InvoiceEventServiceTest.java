package com.tcmp.tiapi.invoice.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tcmp.tiapi.invoice.model.InvoiceEventInfo;
import com.tcmp.tiapi.invoice.repository.redis.InvoiceEventRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InvoiceEventServiceTest {
  @Mock private InvoiceEventRepository invoiceEventRepository;

  @Captor private ArgumentCaptor<String> uuidArgumentCaptor;

  @InjectMocks private InvoiceEventService invoiceEventService;

  @Test
  void findInvoiceEventInfoByUuid_itShouldReturnInvoiceEventInfo() {
    var invoiceInfoUuid = "1-1-1-1";

    when(invoiceEventRepository.findById(anyString()))
        .thenReturn(Optional.of(InvoiceEventInfo.builder().build()));

    assertNotNull(invoiceEventService.findInvoiceEventInfoByUuid(invoiceInfoUuid));
  }

  @Test
  void findInvoiceEventInfoByUuid_itShouldThrowException() {
    when(invoiceEventRepository.findById(anyString())).thenReturn(Optional.empty());

    assertThrows(
        EntityNotFoundException.class, () -> invoiceEventService.findInvoiceEventInfoByUuid(""));
  }

  @Test
  void deleteInvoiceByUuid_itShouldDeleteInvoice() {
    var invoiceUuid = "000-001";

    invoiceEventService.deleteInvoiceByUuid(invoiceUuid);

    verify(invoiceEventRepository).deleteById(uuidArgumentCaptor.capture());
    assertEquals(invoiceUuid, uuidArgumentCaptor.getValue());
  }
}
