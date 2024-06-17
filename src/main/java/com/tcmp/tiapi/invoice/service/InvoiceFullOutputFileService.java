package com.tcmp.tiapi.invoice.service;

import com.opencsv.CSVWriter;
import com.tcmp.tiapi.invoice.exception.InvoiceFileException;
import com.tcmp.tiapi.invoice.model.bulkcreate.InvoiceRowProcessingResult;
import com.tcmp.tiapi.invoice.repository.redis.InvoiceRowProcessingResultRepository;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceFullOutputFileService {
  private static final char SEPARATOR = '\t';

  private final InvoiceRowProcessingResultRepository invoiceRowProcessingResultRepository;
  private final InvoiceFileWriter invoiceFileWriter;

  @Value("${sftp.local-dir.full-output}")
  private String localTempPath;

  /**
   * This function generates the full output file containing the result of the invoice upload
   * processing results.
   *
   * @param originalFilename Filename received by the upload bulk invoices file.
   * @param fileUuid File information uuid, information is stored in redis.
   * @return The absolute path of the created file.
   */
  public String generateAndSaveFile(String originalFilename, String fileUuid) {
    String filename = generateFilename(originalFilename);
    String tempFilePath = localTempPath + filename;

    try (CSVWriter writer = invoiceFileWriter.createWriter(tempFilePath, SEPARATOR)) {
      writer.writeNext(header());

      invoiceRowProcessingResultRepository
          .findAllByFileUuidOrderByIndex(fileUuid)
          .forEach(result -> writer.writeNext(mapResultToRow(result)));

      return tempFilePath;
    } catch (IOException e) {
      throw new InvoiceFileException(e.getMessage());
    }
  }

  private String generateFilename(String originalFilename) {
    String filename = originalFilename.split("\\.")[0];
    return String.format("/%s-FULLOUTPUT.tsv", filename);
  }

  private String[] header() {
    return new String[] {"Índice", "Estado", "Descripción Estado"};
  }

  private String[] mapResultToRow(InvoiceRowProcessingResult result) {
    String codesValue = getErrorCodesValue(result.getErrorCodes());
    return new String[] {
      result.getIndex().toString(),
      InvoiceRowProcessingResult.Status.PENDING == result.getStatus()
          ? "Pendiente"
          : "No Procesado",
      codesValue
    };
  }

  private String getErrorCodesValue(List<String> errorCodes) {
    if (errorCodes == null || errorCodes.isEmpty()) return "";
    return String.join(",", errorCodes);
  }
}
