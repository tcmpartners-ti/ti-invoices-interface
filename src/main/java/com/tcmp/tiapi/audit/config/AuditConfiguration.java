package com.tcmp.tiapi.audit.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcmp.tiapi.audit.filter.RequestTraceLogLoggingFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
public class AuditConfiguration {
  @Bean
  public RequestTraceLogLoggingFilter requestLogFilter(ObjectMapper objectMapper) {
    return new RequestTraceLogLoggingFilter(objectMapper);
  }
}
