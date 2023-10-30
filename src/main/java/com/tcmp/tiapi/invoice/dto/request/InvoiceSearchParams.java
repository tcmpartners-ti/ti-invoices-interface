package com.tcmp.tiapi.invoice.dto.request;

import com.tcmp.tiapi.shared.FieldValidationRegex;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record InvoiceSearchParams(
  @NotNull(message = "This field is required")
  @NotBlank(message = "This filed should not be blank")
  @Size(max = 35, message = "The program id should be up to 35 characters long")
  @Pattern(regexp = FieldValidationRegex.AVOID_SPECIAL_CHARACTERS, message = "Special characters are not allowed")
  @Schema(maxLength = 35, description = "Indicates the credit line to which the invoice relates.")
  String programme,

  @NotNull(message = "This field is required")
  @NotBlank(message = "This filed should not be blank")
  @Size(min = 13, max = 13, message = "The seller mnemonic should be 13 characters long")
  @Pattern(regexp = FieldValidationRegex.ONLY_NUMERIC_VALUES, message = "This field allows numbers only")
  @Schema(maxLength = 13, description = "Seller mnemonic (RUC).")
  String seller,

  @NotNull(message = "This field is required")
  @NotBlank(message = "This filed should not be blank")
  @Size(min = 1, max = 34, message = "Invoice number must be between 1 and 34 characters long")
  @Pattern(regexp = FieldValidationRegex.INVOICE_NUMBER, message = "This field allows numbers and hyphens (-) only")
  @Schema(minLength = 1, maxLength = 34, description = "Invoice reference number .")
  String invoice
) {
}
