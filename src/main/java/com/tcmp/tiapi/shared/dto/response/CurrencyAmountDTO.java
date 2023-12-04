package com.tcmp.tiapi.shared.dto.response;

import com.tcmp.tiapi.shared.FieldValidationRegex;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CurrencyAmountDTO {
  @NotNull(message = "This field is required.")
  @DecimalMin(value = "0", message = "The amount must be positive")
  @DecimalMax(
      value = "99999999999999999999",
      message = "The amount must not exceed 9999999999999999999999.")
  @Schema(description = "Monetary amount.")
  private BigDecimal amount;

  @NotNull(message = "This field is required.")
  @Size(min = 1, max = 3, message = "Currency code must be between 1 and 3 characters")
  @Pattern(regexp = FieldValidationRegex.ONLY_LETTERS, message = "Only letters are allowed")
  @Schema(description = "Currency code.")
  private String currency;
}
