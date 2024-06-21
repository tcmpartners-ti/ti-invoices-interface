package com.tcmp.tiapi.invoice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class InvoicesCreatedDTO {
  @Schema(name = "message", description = "Operation result message.")
  private String message;
}
