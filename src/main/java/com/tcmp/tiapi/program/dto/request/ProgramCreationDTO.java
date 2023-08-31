package com.tcmp.tiapi.program.dto.request;

import com.tcmp.tiapi.shared.dto.response.CurrencyAmountDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProgramCreationDTO {
  @NotBlank(message = "Program id is required.")
  @Size(min = 1, max = 35, message = "Program id must be between 1 and 60 characters.")
  @Schema(description = "Program identifier.", minLength = 1, maxLength = 35)
  private String id;

  @NotBlank(message = "Program description is required.")
  @Size(min = 1, max = 60, message = "Program description must be between 1 and 60 characters.")
  @Schema(description = "Program description.", minLength = 1, maxLength = 60)
  private String description;

  @Valid
  private ProgramCustomerDTO customer;

  @FutureOrPresent(message = "Date must be present or future")
  @Schema(name = "startDate", description = "Program start date in format yyyy-MM-dd.")
  private LocalDate startDate;

  @Future(message = "Date must be in the future")
  @Schema(name = "endDate", description = "Program end date in format yyyy-MM-dd.")
  private LocalDate endDate;

  @Pattern(regexp = "[BS]", message = "Type must be 'B' or 'S' (Buyer or Seller centric)")
  @Schema(name = "type", description = "Program type. B=Buyer centric, S=Seller centric.")
  private String type;

  private CurrencyAmountDTO creditLimit;

  @Pattern(regexp = "[BRIA]", message = "Status must be either 'B', 'R', 'I' or 'A'")
  @Schema(name = "status", description = "Program status. B=Blocked, R=Referred, I=Inactive, A=Active.")
  private String status;
}
