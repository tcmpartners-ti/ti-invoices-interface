package com.tcmp.tiapi.titoapigee.businessbanking;

import com.tcmp.tiapi.titoapigee.security.ApiGeeBodyEncryptionInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class BusinessBankingConfiguration {
  @Value("${bp.api-gee.api-key}") private String apiEncryptionKey;
  @Value("${bp.api-gee.api-secret}") private String apiSecret;

  @Bean
  public ApiGeeBodyEncryptionInterceptor operationalGatewaySigner() {
    return new ApiGeeBodyEncryptionInterceptor(
      apiEncryptionKey,
      apiSecret
    );
  }
}
