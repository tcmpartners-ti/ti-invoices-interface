package com.tcmp.tiapi.invoice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InvoiceProgramDTO {
  @Schema(name = "id", description = "Program id (defined by customer).")
  private String id;

  @Schema(name = "description", description = "Program description.")
  private String description;
}
