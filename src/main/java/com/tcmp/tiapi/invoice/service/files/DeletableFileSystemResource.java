package com.tcmp.tiapi.invoice.service.files;

import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import org.springframework.core.io.FileSystemResource;

public class DeletableFileSystemResource extends FileSystemResource {

  public DeletableFileSystemResource(String path) {
    super(path);
  }

  @Override
  public InputStream getInputStream() throws IOException {
    File file = getFile();
    InputStream inputStream = super.getInputStream();

    return new FilterInputStream(inputStream) {
      @Override
      public void close() throws IOException {
        super.close();
        Files.delete(file.toPath());
      }
    };
  }
}
