package com.tcmp.tiapi.titofcm.config;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "azure")
@Slf4j
public class FcmAzureContainerConfiguration {
  private StorageAccount storageAccount;
  private String user;
  private String host;
  private String port;
  private String privateKey;
  private LocalDir localDir;
  private RemoteDir remoteDir;

  @Bean
  public BlobContainerClient fcmContainerClient() {
    BlobServiceClient client =
        new BlobServiceClientBuilder()
            .connectionString(storageAccount.connectionString())
            .buildClient();

    return client.getBlobContainerClient(storageAccount.fcmContainer());
  }

  public String user() {
    return user;
  }

  public LocalDir localDirectories() {
    return localDir;
  }

  public RemoteDir remoteDirectories() {
    return remoteDir;
  }

  @Data
  @AllArgsConstructor
  public static class StorageAccount {
    private String connectionString;
    private String fcmContainer;

    public String connectionString() {
      return connectionString;
    }

    public String fcmContainer() {
      return fcmContainer;
    }
  }

  @Data
  @AllArgsConstructor
  public static class LocalDir {
    private String OutputDir;

    public String OutputDir() {
      return OutputDir;
    }
  }

  @Data
  @AllArgsConstructor
  public static class RemoteDir {
    private String OutputDir;

    public String OutputDir() { return OutputDir; }
  }
}
