package com.tcmp.tiapi.shared.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CurrencyAmountDTO {
  @NotNull(message = "This field is required.")
  @DecimalMin(value = "0", message = "The amount must be positive")
  @DecimalMax(value = "99999999999999999999", message = "The amount must not exceed 9999999999999999999999.")
  @Schema(description = "Monetary amount.")
  private BigDecimal amount;

  @NotNull(message = "This field is required.")
  @Size(min = 1, max = 3, message = "Currency code must be between 1 and 3 characters")
  @Schema(description = "Currency code.")
  private String currency;
}
