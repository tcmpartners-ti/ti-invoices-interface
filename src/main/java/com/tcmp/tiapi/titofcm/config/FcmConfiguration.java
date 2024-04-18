package com.tcmp.tiapi.titofcm.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "fcm")
@Getter
@Setter
public class FcmConfiguration {
  private ActiveMqConfiguration activemq;

  public record ActiveMqConfiguration(String host, String port, String user, String password) {}
}
