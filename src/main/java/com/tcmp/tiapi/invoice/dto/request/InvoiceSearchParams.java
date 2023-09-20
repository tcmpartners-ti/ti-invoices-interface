package com.tcmp.tiapi.invoice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record InvoiceSearchParams(
  @NotNull
  @Size(max = 35, message = "The program id should be up to 35 characters long")
  @Schema(maxLength = 35, description = "Indicates the credit line to which the invoice relates.")
  String programme,
  @NotNull
  @Size(max = 35, message = "The seller mnemonic should be up to 35 characters long")
  @Schema(maxLength = 35, description = "Seller mnemonic (RUC).")
  String seller,
  @NotNull
  @Size(min = 1, max = 34, message = "Invoice number must be between 1 and 34 characters long")
  @Schema(minLength = 1, maxLength = 34, description = "Invoice reference number .")
  String invoice
) {
}
