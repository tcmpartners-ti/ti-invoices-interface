package com.tcmp.tiapi.program.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tcmp.tiapi.program.dto.request.ProgramCustomerDTO;
import com.tcmp.tiapi.shared.dto.response.CurrencyAmountDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProgramDTO {
  private static final String DATE_FORMAT = "dd-MM-yyyy";

  @Schema(description = "Program identifier.", maxLength = 35, example = "ASEGURADORASUR")
  private String id;

  @Schema(description = "Program description.", maxLength = 60, example = "ASEGURADORASUR")
  private String description;

  private ProgramCustomerDTO customer;

  @JsonFormat(pattern = DATE_FORMAT)
  @Schema(description = "Program start date in format yyyy-MM-dd.")
  private LocalDate startDate;

  @JsonFormat(pattern = DATE_FORMAT)
  @Schema(description = "Program end date in format yyyy-MM-dd.")
  private LocalDate expiryDate;

  @Schema(
      description = "Program type. B=Buyer centric, S=Seller centric.",
      minLength = 1,
      maxLength = 1,
      example = "B")
  private Character type;

  private CurrencyAmountDTO creditLimit;

  @Schema(
      description = "Program status. B=Blocked, R=Referred, I=Inactive, A=Active.",
      minLength = 1,
      maxLength = 1,
      example = "A")
  private Character status;

  @Schema(
      description = "Program extra financing days.",
      minimum = "0",
      defaultValue = "0",
      example = "30")
  private Integer extraFinancingDays;
}
