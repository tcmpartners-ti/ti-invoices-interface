package com.tcmp.tiapi.invoice.service;

import com.opencsv.CSVWriter;
import com.opencsv.ICSVWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class InvoiceFileWriter {
  public CSVWriter createWriter(String path, Character separator) throws FileNotFoundException {
    return new CSVWriter(
        new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8),
        separator,
        ICSVWriter.NO_QUOTE_CHARACTER,
        ICSVWriter.DEFAULT_QUOTE_CHARACTER,
        ICSVWriter.DEFAULT_LINE_END);
  }
}
