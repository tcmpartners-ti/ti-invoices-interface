package com.tcmp.tiapi.titoapigee;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcmp.tiapi.titoapigee.businessbanking.BusinessBankingHeaderSigner;
import com.tcmp.tiapi.titoapigee.paymentexecution.PaymentExecutionHeaderSigner;
import com.tcmp.tiapi.titoapigee.security.HeaderSigner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiGeeConfiguration {
  private static final String DEVICE_IP = "10.0.0.1";
  @Value("${bp.api-gee.headers.device}") private String device;
  @Value("${bp.api-gee.app-id}") private String appId;
  @Value("${bp.api-gee.api-key}") private String apiEncryptionKey;
  @Value("${bp.api-gee.api-secret}") private String apiSecret;

  @Bean
  public HeaderSigner paymentExecutionHeaderSigner() {
    return new PaymentExecutionHeaderSigner(
      new ObjectMapper(),
      appId,
      apiEncryptionKey,
      apiSecret,
      device,
      DEVICE_IP
    );
  }

  @Bean
  public HeaderSigner businessBankingHeaderSigner() {
    return new BusinessBankingHeaderSigner(
      new ObjectMapper(),
      appId,
      apiEncryptionKey,
      apiSecret,
      device,
      DEVICE_IP
    );
  }
}
