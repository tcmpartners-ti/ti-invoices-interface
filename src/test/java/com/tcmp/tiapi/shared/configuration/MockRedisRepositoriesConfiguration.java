package com.tcmp.tiapi.shared.configuration;

import static org.mockito.Mockito.*;

import com.tcmp.tiapi.invoice.repository.redis.InvoiceEventRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("test")
public class MockRedisRepositoriesConfiguration {
  @Bean
  public InvoiceEventRepository mockInvoiceCreationEventRepository() {
    return mock(InvoiceEventRepository.class);
  }
}
