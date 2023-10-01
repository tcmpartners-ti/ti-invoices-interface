package com.tcmp.tiapi.shared;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@Configuration
@Profile("!test")
@EnableRedisRepositories(basePackages = {"com.tcmp.tiapi.invoice.repository.redis"})
public class RedisConfiguration {
}
