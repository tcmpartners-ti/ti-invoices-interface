package com.tcmp.tiapi.invoice.service.files.fulloutput;

import com.opencsv.CSVWriter;
import com.tcmp.tiapi.invoice.exception.InvoiceFileException;
import com.tcmp.tiapi.invoice.model.bulkcreate.InvoiceRowProcessingResult;
import com.tcmp.tiapi.invoice.service.files.InvoiceCsvFileWriter;
import com.tcmp.tiapi.titofcm.config.FcmAzureContainerConfiguration;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceFullOutputFileBuilder {
  private static final char SEPARATOR = '\t';

  private final FcmAzureContainerConfiguration containerConfiguration;
  private final InvoiceCsvFileWriter invoiceCsvFileWriter;

  /**
   * This function generates the full output file containing the result of the invoice upload
   * processing results.
   *
   * @param originalFilename Filename received by the upload bulk invoices file.
   * @param results File information, information is stored in redis.
   * @return The absolute path of the created file.
   */
  public String generateAndSaveFile(
      String originalFilename, List<InvoiceRowProcessingResult> results) {
    String filename = generateFilename(originalFilename);
    String localTempPath = containerConfiguration.localDirectories().fullOutput();
    String tempFilePath = localTempPath + filename;

    try (CSVWriter writer = invoiceCsvFileWriter.createWriter(tempFilePath, SEPARATOR)) {
      writer.writeNext(header());

      for (InvoiceRowProcessingResult result : results) {
        writer.writeNext(mapResultToRow(result));
      }

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
