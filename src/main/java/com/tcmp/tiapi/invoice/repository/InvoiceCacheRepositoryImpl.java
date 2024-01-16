package com.tcmp.tiapi.invoice.repository;

import com.tcmp.tiapi.invoice.model.InvoiceEventInfo;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class InvoiceCacheRepositoryImpl implements InvoiceCacheRepository {
  private final RedisTemplate<String, Object> redisTemplate;

  @Override
  public void saveAll(List<InvoiceEventInfo> invoiceEvents) {
    redisTemplate.executePipelined(
        (RedisCallback<Object>)
            connection -> {
              invoiceEvents.forEach(
                  invoiceEvent -> {
                    Map<String, String> hashFields = new HashMap<>();
                    hashFields.put("_class", invoiceEvent.getClass().getName());
                    hashFields.put("id", invoiceEvent.getId());
                    hashFields.put("batchId", invoiceEvent.getBatchId());
                    hashFields.put("reference", invoiceEvent.getReference());
                    hashFields.put("sellerMnemonic", invoiceEvent.getSellerMnemonic());

                    String key = "InvoiceEvent:" + invoiceEvent.getId();
                    connection
                        .hashCommands()
                        .hMSet(key.getBytes(), converStringMapToBytesMap(hashFields));
                  });

              return null;
            });
  }

  private Map<byte[], byte[]> converStringMapToBytesMap(Map<String, String> hashFields) {
    Map<byte[], byte[]> byteMap = new HashMap<>();

    for (Map.Entry<String, String> entry : hashFields.entrySet()) {
      String key = entry.getKey();
      String value = entry.getValue();

      byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
      byte[] valueBytes = value.getBytes(StandardCharsets.UTF_8);

      byteMap.put(keyBytes, valueBytes);
    }

    return byteMap;
  }
}
