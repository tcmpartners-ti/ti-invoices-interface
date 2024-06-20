package com.tcmp.tiapi.invoice.service;

import com.tcmp.tiapi.invoice.exception.InvoiceFileException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.springframework.stereotype.Component;

/** This class acts as a wrapper for the `Files` class to improve testability. */
@Component
public class InvoiceFileHandler {
  public void saveFile(String path, String content) {
    try {
      Files.writeString(Paths.get(path), content);
    } catch (IOException e) {
      throw new InvoiceFileException(e.getMessage());
    }
  }

  public void deleteFile(String path) {
    try {
      Files.delete(Paths.get(path));
    } catch (IOException e) {
      throw new InvoiceFileException(e.getMessage());
    }
  }
}
