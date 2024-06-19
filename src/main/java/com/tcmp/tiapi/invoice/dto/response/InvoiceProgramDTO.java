package com.tcmp.tiapi.invoice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InvoiceProgramDTO {
  @Schema(description = "Program id (defined by customer).")
  private String id;

  @Schema(description = "Program description.")
  private String description;

  @Schema(description = "Program interest rate.")
  private BigDecimal interestRate;

  @Schema(description = "Program extra financing days.")
  private Integer extraFinancingDays;
}
