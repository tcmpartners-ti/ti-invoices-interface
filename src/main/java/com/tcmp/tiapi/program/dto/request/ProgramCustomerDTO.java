package com.tcmp.tiapi.program.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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
  @NotBlank(message = "Customer mnemonic is required.")
  @Size(min = 1, max = 20, message = "Customer mnemonic must be between 1 and 20 characters.")
  @Schema(name = "mnemonic", description = "Customer mnemonic (RUC).", minLength = 1, maxLength = 20)
  private String mnemonic;
}
