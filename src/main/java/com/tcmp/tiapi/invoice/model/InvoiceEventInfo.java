package com.tcmp.tiapi.invoice.model;

import jakarta.persistence.Id;
import java.io.Serializable;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.redis.core.RedisHash;

@RedisHash("InvoiceEvent")
@Builder
@Getter
@Setter
@ToString
public class InvoiceEventInfo implements Serializable {
  @Id private String id;

  private String batchId;

  private String reference;

  private String sellerMnemonic;
}
