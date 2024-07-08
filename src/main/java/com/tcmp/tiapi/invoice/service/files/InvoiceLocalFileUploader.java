package com.tcmp.tiapi.invoice.service.files;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.tcmp.tiapi.invoice.exception.InvoiceFileException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InvoiceLocalFileUploader {
  private final BlobContainerClient fcmContainerClient;

  /**
   * Uploads a file to azure .
   *
   * @param localPath Local absolute file path. E.g: `/tmp/FILE-FULLOUTPUT.tsv`.
   * @param remotePath Remote absolute file path. E.g.: `/ti/full-output/FILE-FULLOUTPUT.tsv`
   */
  public void uploadFromPath(String localPath, String remotePath) {
    BlobClient blobClient = fcmContainerClient.getBlobClient(remotePath);

    boolean fileAlreadyExists = blobClient.exists();
    if (fileAlreadyExists) {
      throw new InvoiceFileException(String.format("File in %s already exists.", remotePath));
    }

    blobClient.uploadFromFile(localPath);
  }
}
