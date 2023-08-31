package com.tcmp.tiapi.program.dto.response;

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
  @Schema(name = "id", description = "Program identifier.", maxLength = 35)
  private String id;

  @Schema(name = "description", description = "Program description.", maxLength = 60)
  private String description;

  private ProgramCustomerDTO customer;

  @Schema(name = "startDate", description = "Program start date in format yyyy-MM-dd.")
  private LocalDate startDate;

  @Schema(name = "endDate", description = "Program end date in format yyyy-MM-dd.")
  private LocalDate endDate;

  @Schema(name = "type", description = "Program type. B=Buyer centric, S=Seller centric.")
  private String type;

  private CurrencyAmountDTO creditLimit;

  @Schema(name = "status", description = "Program status. B=Blocked, R=Referred, I=Inactive, A=Active.")
  private Character status;
}
