package com.tcmp.tiapi.invoice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InvoiceFinancedDTO {
  @Schema(name = "message", description = "Operation result message.")
  private String message;
}
