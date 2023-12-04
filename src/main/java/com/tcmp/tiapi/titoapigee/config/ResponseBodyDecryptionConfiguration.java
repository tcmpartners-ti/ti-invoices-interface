package com.tcmp.tiapi.titoapigee.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcmp.tiapi.titoapigee.security.ApiGeeResponseBodyDecoder;
import feign.codec.Decoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class ResponseBodyDecryptionConfiguration {
  @Value("${bp.api-gee.credentials.api-key}")
  private String apiKey;

  @Value("${bp.api-gee.credentials.api-secret}")
  private String apiSecret;

  @Bean
  public Decoder bodyDecoder(ObjectMapper objectMapper) {
    return new ApiGeeResponseBodyDecoder(objectMapper, apiKey, apiSecret);
  }
}
