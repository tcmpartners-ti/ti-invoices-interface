package com.tcmp.tiapi.invoice.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.tcmp.tiapi.invoice.InvoiceMapper;
import com.tcmp.tiapi.invoice.dto.InvoiceCreationRowCSV;
import com.tcmp.tiapi.invoice.model.InvoiceEventInfo;
import com.tcmp.tiapi.invoice.repository.InvoiceCacheRepository;
import com.tcmp.tiapi.shared.UUIDGenerator;
import com.tcmp.tiapi.shared.exception.InvalidFileHttpException;
import com.tcmp.tiapi.ti.TIServiceRequestWrapper;
import com.tcmp.tiapi.ti.dto.request.ServiceRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.apache.camel.ProducerTemplate;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class InvoiceBatchOperationsServiceTest {
  @Mock private ProducerTemplate producerTemplate;
  @Mock private InvoiceCacheRepository invoiceCacheRepository;
  @Mock private InvoiceMapper invoiceMapper;
  @Mock private TIServiceRequestWrapper wrapper;
  @Mock private UUIDGenerator uuidGenerator;

  @Captor private ArgumentCaptor<String> invoiceMessageUuidArgumentCaptor;
  @Captor private ArgumentCaptor<List<InvoiceEventInfo>> invoiceInfosArgumentCaptor;

  private static MockMultipartFile mockMultipartFile;

  @InjectMocks private InvoiceBatchOperationsService invoiceBatchOperationsService;

  @BeforeAll
  static void beforeAll() throws IOException {
    var resource = new ClassPathResource("/mock/bulk-invoices.csv");
    var content = resource.getContentAsString(StandardCharsets.UTF_8);

    mockMultipartFile =
        new MockMultipartFile("invoices.csv", content.getBytes(StandardCharsets.UTF_8));
  }

  @BeforeEach
  void injectFields() {
    ReflectionTestUtils.setField(
        invoiceBatchOperationsService, "uriFtiOutgoingFrom", "direct:sendToQueue");
  }

  @Test
  void createInvoicesInTi_WithBusinessBankingChannel_itShouldThrowExceptionWhenFileEmpty() {
    var mockFile = new MockMultipartFile("file.csv", new byte[] {});
    var batchId = "batch123";

    assertThrows(
        InvalidFileHttpException.class,
        () -> invoiceBatchOperationsService.createInvoicesInTiWithBusinessBankingChannel(mockFile, batchId));
  }

  @Test
  void createInvoicesInTi_itShouldSendAllInvoicesToTiWithBusinessBankingChannel() {
    var batchId = "11111";
    var totalInvoices = 220;
    var firstUuid = "000-001";

    var uuids =
        IntStream.rangeClosed(2, totalInvoices)
            .mapToObj(i -> "000-" + String.format("%03d", i))
            .toArray(String[]::new);

    when(wrapper.wrapRequest(any(), any(), any(), any(), any()))
        .thenReturn(ServiceRequest.builder().build());
    when(uuidGenerator.getNewId()).thenReturn(firstUuid, uuids);

    invoiceBatchOperationsService.createInvoicesInTiWithBusinessBankingChannel(mockMultipartFile, batchId);

    // File has 220 invoices and invoices are processed in batches of 100.
    var expectedBatchCalls = 3;
    verify(wrapper, times(totalInvoices))
        .wrapRequest(any(), any(), any(), invoiceMessageUuidArgumentCaptor.capture(), any());
    verify(invoiceMapper, times(totalInvoices))
        .mapCSVRowToFTIMessage(any(InvoiceCreationRowCSV.class), anyString());
    verify(invoiceCacheRepository, times(expectedBatchCalls))
        .saveAll(invoiceInfosArgumentCaptor.capture());
    verify(producerTemplate, times(totalInvoices))
        .asyncSendBody(anyString(), any(ServiceRequest.class));

    var expectedUuids = Stream.concat(Stream.of(firstUuid), Arrays.stream(uuids)).toList();
    var actualMessagesCorrelationUuids = invoiceMessageUuidArgumentCaptor.getAllValues();
    var actualCacheUuids =
        invoiceInfosArgumentCaptor.getAllValues().stream()
            .flatMap(invoices -> invoices.stream().map(InvoiceEventInfo::getId))
            .toList();
    assertEquals(expectedUuids, actualMessagesCorrelationUuids);
    assertEquals(expectedUuids, actualCacheUuids);
  }
}
