package com.tcmp.tiapi.invoice.repository.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisKeyCommands;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class InvoiceRowProcessingResultBatchRepositoryImpl implements InvoiceRowProcessingResultBatchRepository {
  private final RedisTemplate<String, Object> redisTemplate;

  public long totalRowsByIdPattern(String idPattern) {
    long count = 0;
    ScanOptions scanOptions = ScanOptions.scanOptions().match(idPattern).count(100).build();

    Cursor<byte[]> cursor =
        redisTemplate.execute(
            (RedisConnection connection) -> {
              RedisKeyCommands keyCommands = connection.keyCommands();
              return keyCommands.scan(scanOptions);
            });

    try (cursor) {
      while (cursor != null && cursor.hasNext()) {
        String key = new String(cursor.next());

        if (!key.endsWith(":idx")) {
          count++;
        }
      }
    } catch (Exception e) {
      log.error(e.getMessage());
    }

    return count;
  }
}
