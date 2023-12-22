package com.tcmp.tiapi.health;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class HealthService {
  private final DataSource dataSource;
  private final RedisConnectionFactory redisConnectionFactory;
  private final ConnectionFactory jmsConnectionFactory;

  public Map<String, String> checkStatusFromSources() {
    Map<String, String> sourceToStatus = new HashMap<>();
    sourceToStatus.put("db", checkDbStatus().name());
    sourceToStatus.put("redis", checkRedisStatus().name());
    sourceToStatus.put("activeMq", checkQueuesStatus().name());

    return sourceToStatus;
  }

  private HealthStatus checkDbStatus() {
    try {
      Connection connection = dataSource.getConnection();
      return !connection.isClosed() ? HealthStatus.UP : HealthStatus.DOWN;
    } catch (SQLException e) {
      log.error("Could not connect ot database: {}", e.getMessage());
      return HealthStatus.DOWN;
    }
  }

  private HealthStatus checkRedisStatus() {
    try {
      RedisConnection connection = redisConnectionFactory.getConnection();

      String pong =
          Optional.ofNullable(connection.ping())
              .orElseThrow(() -> new RedisConnectionFailureException("No response received."));

      boolean isConnected = pong.equalsIgnoreCase("PONG");
      return isConnected ? HealthStatus.UP : HealthStatus.DOWN;
    } catch (RedisConnectionFailureException e) {
      log.error("Could not connect to Redis: {}", e.getMessage());
      return HealthStatus.DOWN;
    }
  }

  private HealthStatus checkQueuesStatus() {
    try {
      jakarta.jms.Connection connection = jmsConnectionFactory.createConnection();
      connection.close();
      return HealthStatus.UP;
    } catch (JMSException e) {
      log.error("Could not connect to ActiveMQ: {}", e.getMessage());
      return HealthStatus.DOWN;
    }
  }
}
