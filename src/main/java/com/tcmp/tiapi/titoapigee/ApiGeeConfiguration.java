package com.tcmp.tiapi.titoapigee;

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
  @Value("${bp.api-gee.services.business-banking.app-id}") private String businessBankingAppId;
  @Value("${bp.api-gee.services.business-banking.api-key}") private String businessBankingApiKey;
  @Value("${bp.api-gee.services.business-banking.api-secret}") private String businessBankingApiSecret;

  @Value("${bp.api-gee.services.payment-execution.app-id}") private String paymentExecutionAppId;
  @Value("${bp.api-gee.services.payment-execution.api-key}") private String paymentExecutionApiKey;
  @Value("${bp.api-gee.services.payment-execution.api-secret}") private String paymentExecutionApiSecret;

  @Value("${bp.api-gee.services.operational-gateway.app-id}") private String operationalGatewayAppId;
  @Value("${bp.api-gee.services.operational-gateway.api-key}") private String operationalGatewayApiKey;
  @Value("${bp.api-gee.services.operational-gateway.api-secret}") private String operationalGatewayApiSecret;

  @Bean
  @Qualifier("businessBankingHeaderSigner")
  public HeaderSigner businessBankingHeaderSigner() {
    return new EncryptedBodyRequestHeaderSigner(
      new ObjectMapper(),
      businessBankingAppId,
      businessBankingApiKey,
      businessBankingApiSecret,
      device,
      DEVICE_IP
    );
  }

  @Bean
  @Qualifier("paymentExecutionHeaderSigner")
  public HeaderSigner paymentExecutionHeaderSigner() {
    return new PlainBodyRequestHeaderSigner(
      new ObjectMapper(),
      paymentExecutionAppId,
      paymentExecutionApiKey,
      paymentExecutionApiSecret,
      device,
      DEVICE_IP
    );
  }

  @Bean
  @Qualifier("operationalGatewayHeaderSigner")
  public HeaderSigner operationalGatewayHeaderSigner() {
    return new PlainBodyRequestHeaderSigner(
      new ObjectMapper(),
      operationalGatewayAppId,
      operationalGatewayApiKey,
      operationalGatewayApiSecret,
      device,
      DEVICE_IP
    );
  }
}
