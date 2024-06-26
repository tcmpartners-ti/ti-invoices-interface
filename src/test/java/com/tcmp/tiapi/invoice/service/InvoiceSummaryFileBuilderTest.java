package com.tcmp.tiapi.invoice.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.tcmp.tiapi.invoice.model.bulkcreate.BulkCreateInvoicesFileInfo;
import com.tcmp.tiapi.invoice.model.bulkcreate.InvoiceRowProcessingResult;
import com.tcmp.tiapi.invoice.repository.redis.InvoiceRowProcessingResultRepository;
import java.time.LocalDateTime;
import java.util.List;

import com.tcmp.tiapi.invoice.service.files.InvoiceFileHandler;
import com.tcmp.tiapi.invoice.service.files.summary.InvoiceSummaryFileBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class InvoiceSummaryFileBuilderTest {
  private static final String LOCAL_TEMP_PATH = "/tmp";

  @Mock private InvoiceRowProcessingResultRepository invoiceRowProcessingResultRepository;
  @Mock private InvoiceFileHandler invoiceFileHandler;

  @Mock private List<InvoiceRowProcessingResult> mockedSuccessfulInvoices;

  @Captor private ArgumentCaptor<String> contentArgumentCaptor;

  @InjectMocks private InvoiceSummaryFileBuilder invoiceSummaryFileBuilder;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(invoiceSummaryFileBuilder, "localTempPath", LOCAL_TEMP_PATH);
  }

  @Test
  void generateAndSaveFile_itShouldSaveFile() {
    var fileInfo =
        BulkCreateInvoicesFileInfo.builder()
            .id("123")
            .receivedAt(LocalDateTime.of(2024, 2, 8, 0, 0))
            .originalFilename("CRD-ArchivoEmpresaACB01-20240610.csv")
            .totalInvoices(100)
            .build();

    invoiceSummaryFileBuilder.generateAndSaveFile(fileInfo);

    when(invoiceRowProcessingResultRepository.findAllByFileUuidAndStatus(any(), any()))
        .thenReturn(List.of());
    when(invoiceRowProcessingResultRepository.findAllByFileUuidAndStatus(
            anyString(), eq(InvoiceRowProcessingResult.Status.PENDING)))
        .thenReturn(mockedSuccessfulInvoices);
    when(mockedSuccessfulInvoices.size()).thenReturn(45);
    doNothing().when(invoiceFileHandler).saveFile(anyString(), contentArgumentCaptor.capture());

    var actualPath = invoiceSummaryFileBuilder.generateAndSaveFile(fileInfo);

    var expectedPath = "/tmp/CRD-ArchivoEmpresaACB01-20240610-SUMMARY.tsv";
    var expectedContent =
        """
        Resumen del Procesamiento
        Archivo: CRD-ArchivoEmpresaACB01-20240610.csv
        Corte: 20240208 00:00
        Servicio: CRD

        =====================================================================================
        Total de filas: 100
        Total de filas Procesadas Correctamente: 45
        Total de filas Procesadas con Errores: 55
        """;
    assertNotNull(actualPath);
    assertEquals(expectedPath, actualPath);
    assertEquals(expectedContent, contentArgumentCaptor.getValue());
  }
}
