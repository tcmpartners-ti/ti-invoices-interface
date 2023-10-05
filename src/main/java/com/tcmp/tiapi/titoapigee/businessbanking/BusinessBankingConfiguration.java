package com.tcmp.tiapi.titoapigee.businessbanking;

import com.tcmp.tiapi.titoapigee.security.ApiGeeBodyEncryptionInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class BusinessBankingConfiguration {
  @Value("${bp.api-gee.services.business-banking.api-key}") private String businessBankingApiKey;
  @Value("${bp.api-gee.services.business-banking.api-secret}") private String businessBankingApiSecret;

  @Bean
  public ApiGeeBodyEncryptionInterceptor operationalGatewaySigner() {
    return new ApiGeeBodyEncryptionInterceptor(
      businessBankingApiKey,
      businessBankingApiSecret
    );
  }
}
