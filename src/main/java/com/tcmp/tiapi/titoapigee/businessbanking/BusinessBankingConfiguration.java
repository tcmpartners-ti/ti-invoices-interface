package com.tcmp.tiapi.titoapigee.businessbanking;

import com.tcmp.tiapi.titoapigee.security.ApiGeeBodyEncryptionInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class BusinessBankingConfiguration {
  @Value("${bp.api-gee.credentials.api-key}") private String apiKey;
  @Value("${bp.api-gee.credentials.api-secret}") private String apiSecret;

  @Bean
  public ApiGeeBodyEncryptionInterceptor operationalGatewaySigner() {
    return new ApiGeeBodyEncryptionInterceptor(
      apiKey,
      apiSecret
    );
  }
}
