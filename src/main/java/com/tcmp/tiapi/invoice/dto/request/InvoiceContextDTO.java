package com.tcmp.tiapi.invoice.dto.request;

import com.tcmp.tiapi.shared.FieldValidationRegex;
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
  @NotBlank(message = "This field is required")
  @Size(min = 13, max = 13, message = "This field must have 13 characters")
  @Pattern(regexp = FieldValidationRegex.ONLY_NUMERIC_VALUES, message = "Only numeric values are allowed")
  @Pattern(regexp = FieldValidationRegex.AVOID_SPECIAL_CHARACTERS, message = "Special characters are not allowed")
  @Schema(description = "Sender's customer mnemonic.")
  private String customer;

  @NotNull(message = "This field is required.")
  @Size(min = 1, max = 34, message = "Their reference must be between 1 and 34 characters long")
  @Pattern(regexp = FieldValidationRegex.AVOID_SPECIAL_CHARACTERS, message = "Special characters are not allowed")
  @Schema(description = "Sender's reference for this transaction (if known).", minLength = 1, maxLength = 34)
  private String theirReference;

  @NotNull(message = "This field is required.")
  @NotBlank(message = "Behalf of branch is required")
  @Size(min = 1, max = 8, message = "Behalf of branch must be between 1 and 8 characters long")
  @Pattern(regexp = FieldValidationRegex.AVOID_SPECIAL_CHARACTERS, message = "Special characters are not allowed")
  @Schema(description = "The behalf of branch for the transaction.", minLength = 1, maxLength = 8)
  private String behalfOfBranch;
}
