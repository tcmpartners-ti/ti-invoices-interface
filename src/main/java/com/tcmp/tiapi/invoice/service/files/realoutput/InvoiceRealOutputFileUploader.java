package com.tcmp.tiapi.invoice.service.files.realoutput;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.specialized.AppendBlobClient;
import com.tcmp.tiapi.invoice.dto.InvoiceRealOutputData;
import com.tcmp.tiapi.invoice.exception.InvoiceFileException;
import com.tcmp.tiapi.titofcm.config.FcmAzureContainerConfiguration;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InvoiceRealOutputFileUploader {
  private final FcmAzureContainerConfiguration azureContainerConfiguration;
  private final BlobContainerClient fcmContainerClient;

  private final InvoiceRealOutputFileBuilder fileBuilder;

  /** This method is used when the last invoice creation request is processed. */
  public void createHeader(String originalFilename) {
    AppendBlobClient appendClient = buildAppendBlobClient(originalFilename);

    boolean clientAlreadyExists = appendClient.exists();
    if (clientAlreadyExists) {
      throw new InvoiceFileException(
          String.format("Real Output: File %s already exists.", appendClient.getBlobName()));
    }

    appendClient.create();

    byte[] content = fileBuilder.buildHeaderRow().getBytes(StandardCharsets.UTF_8);
    appendClient.appendBlock(new ByteArrayInputStream(content), content.length);

    log.info("Create append invoice status to: {}", appendClient.getBlobName());
  }

  public void appendInvoiceStatusRow(String originalFilename, InvoiceRealOutputData data) {
    AppendBlobClient appendClient = buildAppendBlobClient(originalFilename);

    boolean clientAlreadyExists = appendClient.exists();
    if (!clientAlreadyExists) {
      throw new InvoiceFileException(
          String.format("Real Output: File %s doesn't exist.", appendClient.getBlobName()));
    }

    byte[] content = fileBuilder.buildRow(data).getBytes(StandardCharsets.UTF_8);
    appendClient.appendBlock(new ByteArrayInputStream(content), content.length);

    log.info("Appended invoice status to: {}", appendClient.getBlobName());
  }

  private AppendBlobClient buildAppendBlobClient(String originalFilename) {
    String remoteDir = azureContainerConfiguration.getRemoteDir().OutputDir();
    String remoteFilePath = remoteDir + buildRemoteFilename(originalFilename);
    BlobClient blobClient = fcmContainerClient.getBlobClient(remoteFilePath);

    return blobClient.getAppendBlobClient();
  }

  private static String buildRemoteFilename(String originalFilename) {
    String filenameWithNoExtension = originalFilename.split("\\.")[0];
    return String.format("/%s-REALOUTPUT.tsv", filenameWithNoExtension);
  }
}
