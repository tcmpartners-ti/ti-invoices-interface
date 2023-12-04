package com.tcmp.tiapi.titoapigee.config;

import com.tcmp.tiapi.titoapigee.security.ApiGeeBodyEncryptionInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class RequestBodyEncryptionConfiguration {
  @Value("${bp.api-gee.credentials.api-key}")
  private String apiKey;

  @Value("${bp.api-gee.credentials.api-secret}")
  private String apiSecret;

  @Bean
  public ApiGeeBodyEncryptionInterceptor bodyEncryptor() {
    return new ApiGeeBodyEncryptionInterceptor(apiKey, apiSecret);
  }
}
