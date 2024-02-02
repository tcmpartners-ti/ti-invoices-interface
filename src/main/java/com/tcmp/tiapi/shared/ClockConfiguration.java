package com.tcmp.tiapi.shared;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClockConfiguration {
  @Value("${configuration.zone-id}")
  private String zoneId;

  @Bean
  public Clock clock() {
    return Clock.system(ZoneId.of(zoneId));
  }
}
