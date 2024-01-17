package com.tcmp.tiapi.invoice.repository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.tcmp.tiapi.invoice.model.InvoiceEventInfo;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisHashCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;

@ExtendWith(MockitoExtension.class)
class InvoiceCacheRepositoryImplTest {
  @Spy private RedisTemplate<String, Object> template = new RedisTemplate<>();
  @Mock private RedisConnectionFactory connectionFactoryMock;
  @Mock private RedisConnection connectionMock;
  @Mock private RedisHashCommands hashCommandsMock;

  private InvoiceCacheRepositoryImpl invoiceCacheRepository;

  @BeforeEach
  void setUp() {
    template.setConnectionFactory(connectionFactoryMock);
    when(connectionFactoryMock.getConnection()).thenReturn(connectionMock);
    when(connectionMock.hashCommands()).thenReturn(hashCommandsMock);

    template.afterPropertiesSet();

    invoiceCacheRepository = new InvoiceCacheRepositoryImpl(template);
  }

  @Test
  void saveAll_itShouldProcessAllInvoices() {
    var batchId = "123";
    var invoiceEvents =
        List.of(
            buildMockInvoice(batchId, 1),
            buildMockInvoice(batchId, 2),
            buildMockInvoice(batchId, 3));

    invoiceCacheRepository.saveAll(invoiceEvents);

    verify(template).executePipelined(any(RedisCallback.class));
    verify(hashCommandsMock, times(3)).hMSet(any(byte[].class), anyMap());
  }

  private InvoiceEventInfo buildMockInvoice(String batchId, int i) {
    return InvoiceEventInfo.builder()
        .id(String.format("000-00%d", i))
        .batchId(batchId)
        .reference(String.format("001-000-00%d", i))
        .sellerMnemonic("1722466440001")
        .build();
  }
}
