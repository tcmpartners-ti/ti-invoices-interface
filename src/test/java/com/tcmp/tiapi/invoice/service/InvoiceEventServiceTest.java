package com.tcmp.tiapi.invoice.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.tcmp.tiapi.invoice.model.InvoiceEventInfo;
import com.tcmp.tiapi.invoice.repository.InvoiceRepository;
import com.tcmp.tiapi.invoice.repository.redis.InvoiceEventRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InvoiceEventServiceTest {
  @Mock private InvoiceRepository invoiceRepository;
  @Mock private InvoiceEventRepository invoiceEventRepository;

  private InvoiceEventService testedInvoiceEventService;

  @BeforeEach
  void setUp() {
    testedInvoiceEventService = new InvoiceEventService(invoiceRepository, invoiceEventRepository);
  }

  @Test
  void findInvoiceEventInfoByUuid_itShouldReturnInvoiceEventInfo() {
    String invoiceInfoUuid = "1-1-1-1";

    when(invoiceEventRepository.findById(anyString()))
      .thenReturn(Optional.of(InvoiceEventInfo.builder().build()));

    assertNotNull(testedInvoiceEventService.findInvoiceEventInfoByUuid(invoiceInfoUuid));
  }

  @Test
  void findInvoiceEventInfoByUuid_itShouldThrowException() {
    when(invoiceEventRepository.findById(anyString()))
      .thenReturn(Optional.empty());

    assertThrows(
      EntityNotFoundException.class,
      () -> testedInvoiceEventService.findInvoiceEventInfoByUuid("")
    );
  }
}
