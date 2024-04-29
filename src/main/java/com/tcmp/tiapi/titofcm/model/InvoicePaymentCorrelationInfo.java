package com.tcmp.tiapi.titofcm.model;

import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

/**
 * This class is responsible for maintaining information when FCM asynchronously notifies us of the
 * result (Failure or Success) of a payment. Due to the asynchronous nature of the action, the
 * invoice information needs to be persisted.
 */
@RedisHash("InvoicePaymentCorrelationInfo")
@Data
@Builder
public class InvoicePaymentCorrelationInfo {
  @Id private String id;
  @Indexed private String paymentReference;
  private InitialEvent initialEvent;
  private String eventPayload;

  public enum InitialEvent {
    SETTLEMENT,
    BUYER_CENTRIC_FINANCE_0,
    BUYER_CENTRIC_FINANCE_1,
  }
}
