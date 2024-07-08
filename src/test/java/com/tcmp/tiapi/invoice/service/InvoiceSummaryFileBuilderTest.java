package com.tcmp.tiapi.invoice.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.tcmp.tiapi.invoice.model.bulkcreate.BulkCreateInvoicesFileInfo;
import com.tcmp.tiapi.invoice.service.files.InvoiceFileHandler;
import com.tcmp.tiapi.invoice.service.files.summary.InvoiceSummaryFileBuilder;
import com.tcmp.tiapi.titofcm.config.FcmAzureContainerConfiguration;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InvoiceSummaryFileBuilderTest {
  @Mock private FcmAzureContainerConfiguration containerConfiguration;
  @Mock private InvoiceFileHandler invoiceFileHandler;

  @Captor private ArgumentCaptor<String> contentArgumentCaptor;

  @InjectMocks private InvoiceSummaryFileBuilder invoiceSummaryFileBuilder;

  @BeforeEach
  void setUp() {
    var localDirectories = mock(FcmAzureContainerConfiguration.LocalDir.class);

    when(containerConfiguration.localDirectories()).thenReturn(localDirectories);
    when(localDirectories.summary()).thenReturn("/tmp/summary");
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

    invoiceSummaryFileBuilder.generateAndSaveFile(fileInfo, 10);

    doNothing().when(invoiceFileHandler).saveFile(anyString(), contentArgumentCaptor.capture());

    var actualPath = invoiceSummaryFileBuilder.generateAndSaveFile(fileInfo, 45);

    var expectedPath = "/tmp/summary/CRD-ArchivoEmpresaACB01-20240610-SUMMARY.tsv";
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
