package com.tcmp.tiapi.program.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProgramCustomerDTO {
  @Size(min = 1, max = 20, message = "Customer mnemonic must be between 1 and 20 characters.")
  @Schema(
      description = "Customer mnemonic (RUC).",
      minLength = 1,
      maxLength = 20,
      example = "1722466421001")
  private String mnemonic;

  @Schema(
      description = "Customer commercial trade code.",
      minLength = 3,
      maxLength = 3,
      example = "SDC")
  private String commercialTradeCode;
}
