package com.tcmp.tiapi.invoice.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tcmp.tiapi.shared.FieldValidationRegex;
import com.tcmp.tiapi.shared.annotation.ValidDateFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class InvoiceFinancingDTO {
  private static final String DATE_FORMAT = "dd-MM-yyyy";

  @Valid
  @NotNull(message = "This field is required.")
  @Schema(description = "The invoice context.")
  private InvoiceContextDTO context;

  @NotNull(message = "This field is required.")
  @Size(min = 13, max = 13, message = "This field must have 13 characters")
  @Pattern(
      regexp = FieldValidationRegex.ONLY_NUMERIC_VALUES,
      message = "Only numeric values are allowed")
  @Pattern(
      regexp = FieldValidationRegex.AVOID_SPECIAL_CHARACTERS,
      message = "Special characters are not allowed")
  @Schema(description = "The anchor party's mnemonic.", example = "1722466433001")
  private String anchorParty;

  @NotNull(message = "This field is required.")
  @Size(min = 1, max = 35, message = "This field must be between 1 and 35 characters")
  @Pattern(
      regexp = FieldValidationRegex.AVOID_SPECIAL_CHARACTERS,
      message = "Special characters are not allowed")
  @Schema(
      description = "Indicates the credit line to which the invoice relates.",
      example = "SUPERMAXI")
  private String programme;

  @NotNull(message = "This field is required.")
  @Size(min = 13, max = 13, message = "This field must have 13 characters")
  @Pattern(
      regexp = FieldValidationRegex.ONLY_NUMERIC_VALUES,
      message = "Only numeric values are allowed")
  @Pattern(
      regexp = FieldValidationRegex.AVOID_SPECIAL_CHARACTERS,
      message = "Special characters are not allowed")
  @Schema(
      description = "The customer who is the credit party on the invoice(s).",
      example = "1722466434001")
  private String seller;

  @Size(min = 12, max = 12, message = "This field must have 12 characters")
  @Pattern(
      regexp = FieldValidationRegex.BP_BANK_ACCOUNT,
      message = "This field must begin with AH or CC followed by 10 digits")
  @Schema(
      description = "The seller's account to finance the invoice.",
      examples = {"AH1234567890", "CC0987654321"})
  private String sellerAccount;

  @NotNull(message = "This field is required.")
  @Size(min = 13, max = 13, message = "This field must have 13 characters")
  @Pattern(
      regexp = FieldValidationRegex.ONLY_NUMERIC_VALUES,
      message = "Only numeric values are allowed")
  @Pattern(
      regexp = FieldValidationRegex.AVOID_SPECIAL_CHARACTERS,
      message = "Special characters are not allowed")
  @Schema(description = "The customer who is the debit party on the invoice(s).")
  private String buyer;

  @NotNull(message = "This field is required.")
  @Pattern(
      regexp = FieldValidationRegex.FORMATTED_DATE,
      message = "Date must be in the format " + DATE_FORMAT)
  @ValidDateFormat(message = "Invalid date", pattern = DATE_FORMAT)
  @Schema(
      description = "The date the invoice(s) will become mature.",
      format = DATE_FORMAT,
      type = "date")
  private String maturityDate;

  @NotNull(message = "This field is required.")
  @Size(min = 1, max = 3, message = "Finance currency code must be between 1 and 3 characters")
  @Pattern(regexp = FieldValidationRegex.ONLY_LETTERS, message = "Only letters are allowed")
  @Schema(
      description = "The currency of the advance - the invoice(s) currency is used as the default.")
  private String financeCurrency;

  @NotNull(message = "This field is required.")
  @Schema(
      minimum = "0.1",
      maximum = "100",
      description = "The percentage of the amount to be financed.")
  @DecimalMin(value = "0.1", message = "This field can't be less than 0.1")
  @DecimalMax(value = "100", message = "This field can't be greater than 100")
  @Digits(
      integer = 15,
      fraction = 2,
      message = "Up to 15 integer digits and 2 fractional digits are allowed")
  private BigDecimal financePercent;

  @NotNull(message = "This field is required.")
  @Pattern(
      regexp = FieldValidationRegex.FORMATTED_DATE,
      message = "Date must be in the format " + DATE_FORMAT)
  @ValidDateFormat(message = "Invalid date", pattern = DATE_FORMAT)
  @Schema(
      description = "The start date of the advance - required for interest calculation.",
      format = DATE_FORMAT,
      type = "date")
  private String financeDate;

  @Valid
  @NotNull(message = "This field is required.")
  InvoiceNumberDTO invoice;
}
