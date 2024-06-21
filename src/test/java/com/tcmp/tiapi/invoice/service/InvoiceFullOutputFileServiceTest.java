package com.tcmp.tiapi.invoice.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.opencsv.CSVWriter;
import com.tcmp.tiapi.invoice.model.bulkcreate.InvoiceRowProcessingResult;
import com.tcmp.tiapi.invoice.repository.redis.InvoiceRowProcessingResultRepository;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class InvoiceFullOutputFileServiceTest {
  private static final String LOCAL_TEMP_PATH = "/tmp";

  @Mock private InvoiceRowProcessingResultRepository invoiceRowProcessingResultRepository;
  @Mock private InvoiceFileWriter invoiceFileWriter;

  @Captor private ArgumentCaptor<String[]> fileRowArgumentCaptor;

  @InjectMocks private InvoiceFullOutputFileService invoiceFullOutputFileService;

  @BeforeEach
  void setup() {
    ReflectionTestUtils.setField(invoiceFullOutputFileService, "localTempPath", LOCAL_TEMP_PATH);
  }

  @Test
  void generateAndSaveFile_itShouldGenerateFile() throws FileNotFoundException {
    var originalFilename = "CRD-ArchivoEmpresaACB01-20240610.csv";
    var fileUuid = "abc-123";
    List<InvoiceRowProcessingResult> results =
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

    when(invoiceFileWriter.createWriter(anyString(), anyChar())).thenReturn(writerMock);
    when(invoiceRowProcessingResultRepository.findAllByFileUuidOrderByIndex(anyString()))
        .thenReturn(results);
    doNothing().when(writerMock).writeNext(fileRowArgumentCaptor.capture());

    invoiceFullOutputFileService.generateAndSaveFile(originalFilename, fileUuid);

    var expectedRows = new ArrayList<String[]>();
    expectedRows.add(new String[] {"Índice", "Estado", "Descripción Estado"});
    expectedRows.add(new String[] {"1", "No Procesado", "001,004"});
    expectedRows.add(new String[] {"2", "Pendiente", ""});

    assertArrayEquals(expectedRows.toArray(), fileRowArgumentCaptor.getAllValues().toArray());
  }
}
