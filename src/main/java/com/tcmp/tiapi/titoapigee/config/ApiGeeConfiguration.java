package com.tcmp.tiapi.titoapigee.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcmp.tiapi.titoapigee.security.EncryptedBodyRequestHeaderSigner;
import com.tcmp.tiapi.titoapigee.security.PlainBodyRequestHeaderSigner;
import com.tcmp.tiapi.titoapigee.security.HeaderSigner;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiGeeConfiguration {
  private static final String DEVICE_IP = "10.0.0.1";
  @Value("${bp.api-gee.headers.device}") private String device;

  @Value("${bp.api-gee.credentials.app-id}") private String appId;
  @Value("${bp.api-gee.credentials.api-key}") private String apiKey;
  @Value("${bp.api-gee.credentials.api-secret}") private String apiSecret;

  @Bean
  @Qualifier("encryptedBodyRequestHeaderSigner")
  public HeaderSigner encryptedBodyRequestHeaderSigner(ObjectMapper objectMapper) {
    return new EncryptedBodyRequestHeaderSigner(objectMapper, appId, apiKey, apiSecret, device, DEVICE_IP);
  }

  @Bean
  @Qualifier("plainBodyRequestHeaderSigner")
  public HeaderSigner plainBodyRequestHeaderSigner(ObjectMapper objectMapper) {
    return new PlainBodyRequestHeaderSigner(objectMapper, appId, apiKey, apiSecret, device, DEVICE_IP);
  }
}
