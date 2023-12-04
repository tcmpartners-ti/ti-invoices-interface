package com.tcmp.tiapi.titoapigee.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcmp.tiapi.titoapigee.security.EncryptedBodyRequestHeaderSigner;
import com.tcmp.tiapi.titoapigee.security.HeaderSigner;
import com.tcmp.tiapi.titoapigee.security.PlainBodyRequestHeaderSigner;
import java.net.InetAddress;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class ApiGeeConfiguration {
  private static final String DEFAULT_DEVICE_IP = "10.0.0.1";
  @Value("${bp.api-gee.headers.device}") private String device;

  @Value("${bp.api-gee.credentials.app-id}") private String appId;
  @Value("${bp.api-gee.credentials.api-key}") private String apiKey;
  @Value("${bp.api-gee.credentials.api-secret}") private String apiSecret;

  @Bean
  @Qualifier("encryptedBodyRequestHeaderSigner")
  public HeaderSigner encryptedBodyRequestHeaderSigner(ObjectMapper objectMapper) {
    return new EncryptedBodyRequestHeaderSigner(objectMapper, appId, apiKey, apiSecret, device, getPublicIp());
  }

  @Bean
  @Qualifier("plainBodyRequestHeaderSigner")
  public HeaderSigner plainBodyRequestHeaderSigner(ObjectMapper objectMapper) {
    return new PlainBodyRequestHeaderSigner(objectMapper, appId, apiKey, apiSecret, device, getPublicIp());
  }

  private String getPublicIp(){
    try {
      InetAddress direccionIP = InetAddress.getLocalHost();
      return direccionIP.getHostAddress();
    } catch (java.net.UnknownHostException e) {
       log.info("Could not determine the public IP address.");
       return DEFAULT_DEVICE_IP;
    }
  }

}
