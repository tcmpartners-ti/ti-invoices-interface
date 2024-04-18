package com.tcmp.tiapi.shared.config;

import com.tcmp.tiapi.invoice.repository.redis.InvoiceEventRepository;
import com.tcmp.tiapi.titofcm.repository.InvoicePaymentCorrelationInfoRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@Configuration
@Profile("!test")
@EnableRedisRepositories(
    basePackageClasses = {
      InvoiceEventRepository.class,
      InvoicePaymentCorrelationInfoRepository.class
    })
public class RedisConfiguration {
  @Bean
  public RedisTemplate<String, Object> redisTemplate(
      RedisConnectionFactory redisConnectionFactory) {
    RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
    redisTemplate.setConnectionFactory(redisConnectionFactory);

    return redisTemplate;
  }
}
