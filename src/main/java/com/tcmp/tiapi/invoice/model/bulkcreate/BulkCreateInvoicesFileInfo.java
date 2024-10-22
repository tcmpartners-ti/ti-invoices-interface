package com.tcmp.tiapi.invoice.model.bulkcreate;

import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.redis.core.RedisHash;

/**
 * This class stores temporarily the uploaded file metadata to be consumed later when the full
 * output and summary files are built.
 */
@RedisHash("InvoiceFile")
@Builder
@Getter
@Setter
@ToString
public class BulkCreateInvoicesFileInfo {
  // Correlation id for the invoice file
  @Id private String id;

  private Integer totalInvoices;

  private String originalFilename;

  private LocalDateTime receivedAt;

  private String customerCif;
}
