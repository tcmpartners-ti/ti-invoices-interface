package com.tcmp.tiapi.program.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tcmp.tiapi.program.dto.request.ProgramCustomerDTO;
import com.tcmp.tiapi.shared.dto.response.CurrencyAmountDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProgramDTO {
  private static final String DATE_FORMAT = "dd-MM-yyyy";

  @Schema(description = "Program identifier.", maxLength = 35)
  private String id;

  @Schema(description = "Program description.", maxLength = 60)
  private String description;

  private ProgramCustomerDTO customer;

  @JsonFormat(pattern = DATE_FORMAT)
  @Schema(description = "Program start date in format yyyy-MM-dd.")
  private LocalDate startDate;

  @JsonFormat(pattern = DATE_FORMAT)
  @Schema(description = "Program end date in format yyyy-MM-dd.")
  private LocalDate expiryDate;

  @Schema(description = "Program type. B=Buyer centric, S=Seller centric.")
  private String type;

  private CurrencyAmountDTO creditLimit;

  @Schema(description = "Program status. B=Blocked, R=Referred, I=Inactive, A=Active.")
  private Character status;
}
