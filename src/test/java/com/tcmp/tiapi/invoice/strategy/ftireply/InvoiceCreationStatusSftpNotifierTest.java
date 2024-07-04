package com.tcmp.tiapi.invoice.strategy.ftireply;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

import com.tcmp.tiapi.invoice.model.bulkcreate.BulkCreateInvoicesFileInfo;
import com.tcmp.tiapi.invoice.model.bulkcreate.InvoiceRowProcessingResult;
import com.tcmp.tiapi.invoice.repository.redis.BulkCreateInvoicesFileInfoRepository;
import com.tcmp.tiapi.invoice.repository.redis.InvoiceProcessingRowBulkRepository;
import com.tcmp.tiapi.invoice.repository.redis.InvoiceRowProcessingResultRepository;
import com.tcmp.tiapi.invoice.service.files.InvoiceFileHandler;
import com.tcmp.tiapi.invoice.service.files.fulloutput.InvoiceFullOutputFileBuilder;
import com.tcmp.tiapi.invoice.service.files.summary.InvoiceSummaryFileBuilder;
import com.tcmp.tiapi.ti.dto.response.Details;
import com.tcmp.tiapi.ti.dto.response.ResponseHeader;
import com.tcmp.tiapi.ti.dto.response.ResponseStatus;
import com.tcmp.tiapi.ti.dto.response.ServiceResponse;
import com.tcmp.tiapi.titofcm.config.FcmAzureContainerConfiguration;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InvoiceCreationStatusSftpNotifierTest {
  @Mock private FcmAzureContainerConfiguration containerConfiguration;
  @Mock private BulkCreateInvoicesFileInfoRepository bulkCreateInvoicesFileInfoRepository;
  @Mock private InvoiceProcessingRowBulkRepository invoiceProcessingRowBulkRepository;
  @Mock private InvoiceRowProcessingResultRepository invoiceRowProcessingResultRepository;
  @Mock private InvoiceFileHandler invoiceFileHandler;
  @Mock private InvoiceFullOutputFileBuilder invoiceFullOutputFileBuilder;
  @Mock private InvoiceSummaryFileBuilder invoiceSummaryFileBuilder;

  @Captor private ArgumentCaptor<InvoiceRowProcessingResult> resultArgumentCaptor;

  @InjectMocks private InvoiceCreationStatusSftpNotifier invoiceCreationStatusNotifier;

  @BeforeEach
  void setup() {
    var remoteDirectories = mock(FcmAzureContainerConfiguration.RemoteDir.class);

    when(remoteDirectories.summary()).thenReturn("/ti/summary");
    when(remoteDirectories.fullOutput()).thenReturn("/ti/full-output");
    when(containerConfiguration.remoteDirectories()).thenReturn(remoteDirectories);
  }

  @Test
  void notify_itShouldStoreInvoiceInformation() {
    var header =
        ResponseHeader.builder()
            .status(ResponseStatus.SUCCESS.getValue())
            .correlationId("abc-123:1")
            .details(
                new Details(
                    List.of(
                        "Duplicate invoice number - 123",
                        "Invalid programme / seller / buyer relationship",
                        "Invalid outstanding amount",
                        "Invalid programme",
                        "Event aborted Create Invoice in master",
                        "Invalid anchor party",
                        "Invalid seller",
                        "Invalid buyer",
                        "Invalid invoice number",
                        "Invalid issue date",
                        "Invalid settlement date",
                        "Invalid Face Value",
                        "Issue date is later than settlement date",
                        "There is a dependency - Seller / buyer",
                        "Invoice amount exceeds buyer limit availability (BuyerExpos)"),
                    null,
                    null))
            .build();
    var serviceResponse = ServiceResponse.builder().responseHeader(header).build();

    var fileInfo =
        BulkCreateInvoicesFileInfo.builder()
            .id("abc-123")
            .totalInvoices(1)
            .originalFilename("CRD-ArchivoEmpresaACB01-20240610.csv")
            .build();
    when(bulkCreateInvoicesFileInfoRepository.findById(anyString()))
        .thenReturn(Optional.of(fileInfo));
    when(invoiceProcessingRowBulkRepository.totalRowsByIdPattern(anyString())).thenReturn(1L);
    when(invoiceFullOutputFileBuilder.generateAndSaveFile(anyString(), anyList()))
        .thenReturn("/tmp/CRD-ArchivoEmpresaACB01-20240610-FULLOUTPUT.tsv");
    when(invoiceSummaryFileBuilder.generateAndSaveFile(any(), anyLong()))
        .thenReturn("/tmp/CRD-ArchivoEmpresaACB01-20240610-SUMMARY.tsv");

    invoiceCreationStatusNotifier.notify(serviceResponse);

    verify(invoiceRowProcessingResultRepository).save(resultArgumentCaptor.capture());
    verify(invoiceFileHandler, times(2)).deleteFile(anyString());
    verify(bulkCreateInvoicesFileInfoRepository).findById(anyString());
    verify(invoiceRowProcessingResultRepository).deleteAllByFileUuid(anyString());

    var expectedErrorCodes =
        List.of(
                "001", "002", "003", "004", "005", "006", "007", "008", "009", "010", "011", "012",
                "014", "016", "017")
            .toArray();
    var actualErrorCodes = resultArgumentCaptor.getValue().getErrorCodes().toArray();
    assertArrayEquals(expectedErrorCodes, actualErrorCodes);
  }
}
