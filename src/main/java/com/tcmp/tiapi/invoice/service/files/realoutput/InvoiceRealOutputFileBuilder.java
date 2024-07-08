package com.tcmp.tiapi.invoice.service.files.realoutput;

import com.opencsv.CSVWriter;
import com.tcmp.tiapi.invoice.dto.InvoiceRealOutputData;
import com.tcmp.tiapi.invoice.exception.InvoiceFileException;
import java.io.IOException;
import java.io.StringWriter;
import java.time.format.DateTimeFormatter;

import com.tcmp.tiapi.invoice.service.files.InvoiceCsvFileWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InvoiceRealOutputFileBuilder {
  private static final Character SEPARATOR = '\t';
  private static final String DATETIME_FORMAT = "yyyyMMdd hh:mm";

  private final InvoiceCsvFileWriter invoiceCsvFileWriter;

  public String buildHeaderRow() {
    String[] header =
        new String[] {"FACTURA", "FECHA Y HORA", "ESTATUS", "MONTO", "IDENTIFICACIÓN"};

    try (StringWriter stringWriter = new StringWriter();
        CSVWriter csvWriter = invoiceCsvFileWriter.createStringWriter(stringWriter, SEPARATOR)) {
      csvWriter.writeNext(header);

      return stringWriter.toString();
    } catch (IOException e) {
      throw new InvoiceFileException("Error building header in real output file.");
    }
  }

  public String buildRow(InvoiceRealOutputData data) {
    try (StringWriter stringWriter = new StringWriter();
        CSVWriter csvWriter = invoiceCsvFileWriter.createStringWriter(stringWriter, SEPARATOR)) {
      csvWriter.writeNext(mapDataToRow(data));

      return stringWriter.toString();
    } catch (IOException e) {
      throw new InvoiceFileException("Error building row in real output file.");
    }
  }

  private String[] mapDataToRow(InvoiceRealOutputData data) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATETIME_FORMAT);

    return new String[] {
      data.invoiceReference(),
      formatter.format(data.processedAt()),
      data.status().tsvValue(),
      data.amount().toString(),
      data.counterPartyMnemonic()
    };
  }
}
