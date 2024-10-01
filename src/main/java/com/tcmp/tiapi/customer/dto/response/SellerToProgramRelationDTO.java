package com.tcmp.tiapi.customer.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SellerToProgramRelationDTO {
  @DecimalMin(value = "0", message = "The amount must be positive")
  @DecimalMax(
      value = "99999999999999999999",
      message = "The amount must not exceed 9999999999999999999999.")
  @Schema
  private BigDecimal limitAmount;

  @Schema(
      description = "Maximum days of advance payment Seller.",
      minimum = "0",
      defaultValue = "0",
      example = "30")
  private Integer maxNumAdvance;

  @Schema(minimum = "0.1", maximum = "100", description = "Maximum percentage of advance by Seller")
  @DecimalMin(value = "0.1", message = "This field can't be less than 0.1")
  @DecimalMax(value = "100", message = "This field can't be greater than 100")
  @Digits(
      integer = 15,
      fraction = 2,
      message = "Up to 15 integer digits and 2 fractional digits are allowed")
  private BigDecimal maxPercentAdvance;
}
