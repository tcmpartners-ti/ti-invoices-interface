package com.tcmp.tiapi.invoice.model;

import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;

@RedisHash("InvoiceCreation")
@Builder
@Getter
@Setter
@ToString
public class InvoiceCreationEventInfo implements Serializable {
  @Id private String id;

  private String batchId;

  private String reference;

  private String sellerMnemonic;
}
