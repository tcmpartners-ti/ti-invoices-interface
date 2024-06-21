package com.tcmp.tiapi.invoice.model.bulkcreate;

import jakarta.persistence.Id;
import java.util.List;
import lombok.*;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

/**
 * This class is used to store the invoice creation result in memory. Afterward, this information
 * will be used to build the full output file with all the processing information.
 */
@RedisHash("InvoiceRowProcessingResult")
@Builder
@Getter
@Setter
@ToString
public class InvoiceRowProcessingResult {
  @Id private String id;

  @Indexed private String fileUuid;

  private Integer index;

  @Indexed private Status status;

  private List<String> errorCodes;

  public enum Status {
    PENDING,
    NOT_PROCESSED
  }
}
