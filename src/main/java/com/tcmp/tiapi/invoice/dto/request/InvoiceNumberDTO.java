package com.tcmp.tiapi.invoice.dto.request;

import com.tcmp.tiapi.shared.FieldValidationRegex;
import com.tcmp.tiapi.shared.annotation.ValidDateFormat;
import com.tcmp.tiapi.shared.dto.response.CurrencyAmountDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
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
public class InvoiceNumberDTO {
  private static final String DATE_FORMAT = "dd-MM-yyyy";

  @NotNull(message = "This field is required.")
  @Size(min = 1, max = 34, message = "Invoice number should be between 1 and 34 characters.")
  @Pattern(
      regexp = FieldValidationRegex.INVOICE_NUMBER,
      message = "This field allows numbers and hyphens (-) only")
  @Schema(description = "The invoice number of the invoice to be financed.")
  private String number;

  @NotNull(message = "This field is required.")
  @Pattern(
      regexp = FieldValidationRegex.FORMATTED_DATE,
      message = "Date must be in the format " + DATE_FORMAT)
  @ValidDateFormat(message = "Invalid date", pattern = DATE_FORMAT)
  @Schema(description = "The issue date of the invoice to be financed.", format = DATE_FORMAT)
  private String issueDate;

  @Valid
  @NotNull(message = "This field is required.")
  private CurrencyAmountDTO outstanding;
}
