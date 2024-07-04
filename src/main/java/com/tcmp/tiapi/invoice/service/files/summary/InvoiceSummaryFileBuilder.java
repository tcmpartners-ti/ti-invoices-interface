package com.tcmp.tiapi.invoice.service.files.summary;

import com.tcmp.tiapi.invoice.model.bulkcreate.BulkCreateInvoicesFileInfo;
import com.tcmp.tiapi.invoice.service.files.InvoiceFileHandler;
import com.tcmp.tiapi.titofcm.config.FcmAzureContainerConfiguration;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InvoiceSummaryFileBuilder {
  private static final String DATETIME_FORMAT = "yyyyMMdd HH:mm";

  private final FcmAzureContainerConfiguration containerConfiguration;
  private final InvoiceFileHandler invoiceFileHandler;

  /**
   * @param fileInfo The redis entity with the file information.
   * @return The absolute path of the created file.
   */
  public String generateAndSaveFile(
      BulkCreateInvoicesFileInfo fileInfo, long totalInvoicesSucceeded) {
    String fileContent =
        generateHeader(fileInfo) + generateFileBody(fileInfo, totalInvoicesSucceeded);
    String fileName = generateFilename(fileInfo.getOriginalFilename());
    String tempFilePath = containerConfiguration.localDirectories().summary() + fileName;

    invoiceFileHandler.saveFile(tempFilePath, fileContent);

    return tempFilePath;
  }

  private String generateHeader(BulkCreateInvoicesFileInfo fileInfo) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATETIME_FORMAT);
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

  private String generateFileBody(
      BulkCreateInvoicesFileInfo fileInfo, long totalInvoicesSucceeded) {
    int totalInvoices = fileInfo.getTotalInvoices();
    long totalInvoiceFailed = totalInvoices - totalInvoicesSucceeded;

    return """
    =====================================================================================
    Total de filas: %d
    Total de filas Procesadas Correctamente: %d
    Total de filas Procesadas con Errores: %d
    """
        .formatted(totalInvoices, totalInvoicesSucceeded, totalInvoiceFailed);
  }

  private String generateFilename(String originalFilename) {
    String filename = originalFilename.split("\\.")[0];
    return String.format("/%s-SUMMARY.tsv", filename);
  }
}
