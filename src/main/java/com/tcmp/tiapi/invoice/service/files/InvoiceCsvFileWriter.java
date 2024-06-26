package com.tcmp.tiapi.invoice.service.files;

import com.opencsv.CSVWriter;
import com.opencsv.ICSVWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Component;

@Component
public class InvoiceCsvFileWriter {
  public CSVWriter createWriter(String path, Character separator) throws FileNotFoundException {
    return new CSVWriter(
        new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8),
        separator,
        ICSVWriter.NO_QUOTE_CHARACTER,
        ICSVWriter.DEFAULT_QUOTE_CHARACTER,
        ICSVWriter.DEFAULT_LINE_END);
  }

  public CSVWriter createStringWriter(StringWriter stringWriter, Character separator) {
    return new CSVWriter(
        stringWriter,
        separator,
        ICSVWriter.NO_QUOTE_CHARACTER,
        ICSVWriter.DEFAULT_QUOTE_CHARACTER,
        ICSVWriter.DEFAULT_LINE_END);
  }
}
