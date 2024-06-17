package com.tcmp.tiapi.invoice.service;

import com.tcmp.tiapi.invoice.model.bulkcreate.BulkCreateInvoicesFileInfo;
import com.tcmp.tiapi.invoice.model.bulkcreate.InvoiceRowProcessingResult;
import com.tcmp.tiapi.invoice.repository.redis.InvoiceRowProcessingResultRepository;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceSummaryFileService {
  private static final String FILE_RECEIVED_AT_DATETIME_FORMAT = "yyyyMMdd HH:mm";

  private final InvoiceRowProcessingResultRepository invoiceRowProcessingResultRepository;
  private final InvoiceFileHandler invoiceFileHandler;

  @Value("${sftp.local-dir.summary}")
  private String localTempPath;

  /**
   * @param fileInfo The redis entity with the file information.
   * @return The absolute path of the created file.
   */
  public String generateAndSaveFile(BulkCreateInvoicesFileInfo fileInfo) {
    String fileContent = generateHeader(fileInfo) + generateFileBody(fileInfo);
    String fileName = generateFilename(fileInfo.getOriginalFilename());
    String tempFilePath = localTempPath + fileName;

    invoiceFileHandler.saveFile(tempFilePath, fileContent);

    return tempFilePath;
  }

  private String generateFilename(String originalFilename) {
    String filename = originalFilename.split("\\.")[0];
    return String.format("/%s-SUMMARY.tsv", filename);
  }

  private String generateHeader(BulkCreateInvoicesFileInfo fileInfo) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(FILE_RECEIVED_AT_DATETIME_FORMAT);
    String filename = fileInfo.getOriginalFilename();
    String service = filename.split("-")[0];
    String receivedAt = formatter.format(fileInfo.getReceivedAt());

    return """
    Resumen del Procesamiento
    Archivo: %s
    Corte: %s
    Servicio: %s

    """
        .formatted(filename, receivedAt, service);
  }

  private String generateFileBody(BulkCreateInvoicesFileInfo fileInfo) {
    int totalInvoices = fileInfo.getTotalInvoices();
    long totalInvoicesSucceeded =
        invoiceRowProcessingResultRepository
            .findAllByFileUuidAndStatus(fileInfo.getId(), InvoiceRowProcessingResult.Status.PENDING)
            .size();
    long totalInvoiceFailed = totalInvoices - totalInvoicesSucceeded;

    return """
    =====================================================================================
    Total de filas: %d
    Total de filas Procesadas Correctamente: %d
    Total de filas Procesadas con Errores: %d
    """
        .formatted(totalInvoices, totalInvoicesSucceeded, totalInvoiceFailed);
  }
}
