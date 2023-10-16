package com.tcmp.tiapi.invoice.dto.request;

import com.tcmp.tiapi.shared.annotation.ValidDateFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceFinancingDTO {
  private static final String DATE_FORMAT = "dd-MM-yyyy";
  private static final String DATE_REGEX = "^(0[1-9]|[1-2][0-9]|3[0-1])-(0[1-9]|1[0-2])-\\d{4}$";

  @Valid
  @NotNull(message = "This field is required.")
  @Schema(description = "The invoice context.")
  private InvoiceContextDTO context;

  @NotNull(message = "This field is required.")
  @Size(max = 35, message = "The program id should be up to 35 characters long")
  @Schema(maxLength = 35, description = "Indicates the credit line to which the invoice relates.")
  private String programme;

  @NotNull(message = "This field is required.")
  @Size(min = 1, max = 20, message = "Credited party id must be between 1 and 20 characters long")
  @Pattern(regexp = "^\\d+$", message = "Only numeric values are allowed")
  @Schema(minLength = 1, maxLength = 20, description = "The customer who is the credit party on the invoice(s).")
  private String seller;

  @NotNull(message = "This field is required.")
  @Size(min = 1, max = 20, message = "Debited party id must be between 1 and 20 characters long")
  @Pattern(regexp = "^\\d+$", message = "Only numeric values are allowed")
  @Schema(minLength = 1, maxLength = 20, description = "The customer who is the debit party on the invoice(s).")
  private String buyer;

  @NotNull(message = "This field is required.")
  @Size(min = 1, max = 20, message = "The anchor party's mnemonic should be between 1 and 20 characters long")
  @Pattern(regexp = "^\\d+$", message = "Only numeric values are allowed")
  @Schema(description = "The anchor party's mnemonic.", minLength = 1, maxLength = 20)
  private String anchorParty;

  @NotNull(message = "This field is required.")
  @Pattern(regexp = DATE_REGEX, message = "Date must be in the format " + DATE_FORMAT)
  @ValidDateFormat(message = "Invalid date", pattern = DATE_FORMAT)
  @Schema(description = "The date the invoice(s) will become mature.", format = DATE_FORMAT, type = "date")
  private String maturityDate;

  @NotNull(message = "This field is required.")
  @Size(min = 1, max = 3, message = "Finance currency code must be between 1 and 3 characters.")
  @Schema(minLength = 1, maxLength = 3, description = "The currency of the advance - the invoice(s) currency is used as the default.")
  private String financeCurrency;

  @NotNull(message = "This field is required.")
  @Schema(minimum = "0.1", maximum = "100", description = "The percentage of the amount to be financed.")
  @DecimalMin(value = "0.1", message = "This field can't be less than 0.1")
  @DecimalMax(value = "100", message = "This field can't be greater than 100")
  @Digits(integer = 15, fraction = 2, message = "Up to 15 integer digits and 2 fractional digits are allowed")
  private BigDecimal financePercent;

  @NotNull(message = "This field is required.")
  @Pattern(regexp = DATE_REGEX, message = "Date must be in the format " + DATE_FORMAT)
  @ValidDateFormat(message = "Invalid date", pattern = DATE_FORMAT)
  @Schema(description = "The start date of the advance - required for interest calculation.", format = DATE_FORMAT, type = "date")
  private String financeDate;

  @Valid
  @NotNull(message = "This field is required.")
  InvoiceNumberDTO invoice;
}
