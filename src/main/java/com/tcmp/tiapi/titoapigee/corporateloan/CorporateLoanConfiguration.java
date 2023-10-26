package com.tcmp.tiapi.titoapigee.corporateloan;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcmp.tiapi.titoapigee.security.ApiGeeResponseBodyDecoder;
import com.tcmp.tiapi.titoapigee.security.ApiGeeBodyEncryptionInterceptor;
import feign.codec.Decoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CorporateLoanConfiguration {
  @Value("${bp.api-gee.credentials.api-key}") private String apiKey;
  @Value("${bp.api-gee.credentials.api-secret}") private String apiSecret;

  @Bean
  public ApiGeeBodyEncryptionInterceptor requestBodyInterceptor() {
    return new ApiGeeBodyEncryptionInterceptor(
      apiKey,
      apiSecret
    );
  }

  @Bean
  public Decoder bodyDecryptionDecoder(ObjectMapper objectMapper) {
    return new ApiGeeResponseBodyDecoder(
      objectMapper,
      apiKey,
      apiSecret
    );
  }
}
