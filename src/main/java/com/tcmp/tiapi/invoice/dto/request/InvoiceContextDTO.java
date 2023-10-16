package com.tcmp.tiapi.invoice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceContextDTO {
  @NotNull(message = "This field is required.")
  @NotBlank(message = "Customer's mnemonic is required")
  @Size(min = 1, max = 20, message = "Customer's mnemonic must be between 1 and 20 characters long")
  @Pattern(regexp = "^\\d+$", message = "Only numeric values are allowed")
  @Schema(description = "Sender's customer mnemonic.", minLength = 1, maxLength = 20)
  private String customer;

  @NotNull(message = "This field is required.")
  @Size(min = 1, max = 34, message = "Their reference must be between 1 and 34 characters long")
  @Schema(description = "Sender's reference for this transaction (if known).", minLength = 1, maxLength = 34)
  private String theirReference;

  @NotNull(message = "This field is required.")
  @NotBlank(message = "Behalf of branch is required")
  @Size(min = 1, max = 8, message = "Behalf of branch must be between 1 and 8 characters long")
  @Schema(description = "The behalf of branch for the transaction.", minLength = 1, maxLength = 8)
  private String behalfOfBranch;
}
