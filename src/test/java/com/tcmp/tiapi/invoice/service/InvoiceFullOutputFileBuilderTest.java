package com.tcmp.tiapi.invoice.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.opencsv.CSVWriter;
import com.tcmp.tiapi.invoice.model.bulkcreate.InvoiceRowProcessingResult;
import com.tcmp.tiapi.invoice.service.files.InvoiceCsvFileWriter;
import com.tcmp.tiapi.invoice.service.files.fulloutput.InvoiceFullOutputFileBuilder;
import com.tcmp.tiapi.titofcm.config.FcmAzureContainerConfiguration;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InvoiceFullOutputFileBuilderTest {
  @Mock private FcmAzureContainerConfiguration containerConfiguration;
  @Mock private InvoiceCsvFileWriter invoiceCsvFileWriter;

  @Captor private ArgumentCaptor<String[]> fileRowArgumentCaptor;

  @InjectMocks private InvoiceFullOutputFileBuilder invoiceFullOutputFileBuilder;

  @BeforeEach
  void setUp() {
    var localDirectories = mock(FcmAzureContainerConfiguration.LocalDir.class);

    when(containerConfiguration.localDirectories()).thenReturn(localDirectories);
    when(localDirectories.OutputDir()).thenReturn("/fti/out");
  }

  @Test
  void generateAndSaveFile_itShouldGenerateFile() throws FileNotFoundException {
    var originalFilename = "CRD-ArchivoEmpresaACB01-20240610.csv";
    var customerCif = "000000";
    var fileUuid = "abc-123";
    var results =
        List.of(
            InvoiceRowProcessingResult.builder()
                .index(1)
                .fileUuid(fileUuid)
                .status(InvoiceRowProcessingResult.Status.NOT_PROCESSED)
                .errorCodes(List.of("001", "004"))
                .build(),
            InvoiceRowProcessingResult.builder()
                .index(2)
                .fileUuid(fileUuid)
                .status(InvoiceRowProcessingResult.Status.PENDING)
                .build());

    var writerMock = Mockito.mock(CSVWriter.class);

    when(invoiceCsvFileWriter.createWriter(anyString(), anyChar())).thenReturn(writerMock);
    doNothing().when(writerMock).writeNext(fileRowArgumentCaptor.capture());

    invoiceFullOutputFileBuilder.generateAndSaveFile(originalFilename, customerCif, results);

    var expectedRows = new ArrayList<String[]>();
    expectedRows.add(new String[] {"Índice", "Estado", "Descripción Estado"});
    expectedRows.add(new String[] {"1", "No Procesado", "001,004"});
    expectedRows.add(new String[] {"2", "Pendiente", ""});

    assertArrayEquals(expectedRows.toArray(), fileRowArgumentCaptor.getAllValues().toArray());
  }
}
