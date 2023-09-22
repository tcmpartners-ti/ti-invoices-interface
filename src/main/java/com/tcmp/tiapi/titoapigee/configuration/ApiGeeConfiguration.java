package com.tcmp.tiapi.titoapigee.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcmp.tiapi.titoapigee.client.ApiGeeBodyEncryptionInterceptor;
import com.tcmp.tiapi.titoapigee.client.ApiGeeHeaderSigner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
public class ApiGeeConfiguration {
  @Value("${bp.api-gee.headers.device}") private String device;
  @Value("${bp.api-gee.app-id}") private String appId;
  @Value("${bp.api-gee.api-key}") private String apiEncryptionKey;
  @Value("${bp.api-gee.api-secret}") private String apiSecret;

  @Bean
  public ApiGeeHeaderSigner headerSigner() {
    String deviceIp = "10.0.0.1";
    String session = UUID.randomUUID().toString();

    return new ApiGeeHeaderSigner(
      new ObjectMapper(),
      device,
      deviceIp,
      session,
      appId,
      apiEncryptionKey,
      apiSecret
    );
  }

  @Bean
  public ApiGeeBodyEncryptionInterceptor operationalGatewaySigner() {
    return new ApiGeeBodyEncryptionInterceptor(
      apiEncryptionKey,
      apiSecret
    );
  }
}
