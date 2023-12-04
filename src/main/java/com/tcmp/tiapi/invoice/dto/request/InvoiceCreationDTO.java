package com.tcmp.tiapi.invoice.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class InvoiceCreationDTO {
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
  @Schema(description = "The anchor party's mnemonic.")
  private String anchorParty;

  @NotNull(message = "This field is required")
  @Size(min = 12, max = 12, message = "This field must have 12 characters")
  @Pattern(
      regexp = FieldValidationRegex.AVOID_SPECIAL_CHARACTERS,
      message = "Special characters are not allowed")
  @Pattern(
      regexp = FieldValidationRegex.BP_BANK_ACCOUNT,
      message = "This field must begin with AH or CC followed by 10 digits")
  @Schema(
      description = "Anchor account to debit the invoice payment.",
      examples = {"AH1234567890", "CC0987654321"})
  private String anchorAccount;

  @NotNull(message = "This field is required")
  @Size(min = 1, max = 35, message = "This field must be between 1 and 35 characters")
  @Pattern(
      regexp = FieldValidationRegex.AVOID_SPECIAL_CHARACTERS,
      message = "Special characters are not allowed")
  @Schema(description = "Indicates the credit line to which the invoice relates.")
  private String programme;

  @NotNull(message = "This field is required.")
  @Size(min = 13, max = 13, message = "This field must have 13 characters")
  @Pattern(
      regexp = FieldValidationRegex.ONLY_NUMERIC_VALUES,
      message = "Only numeric values are allowed")
  @Pattern(
      regexp = FieldValidationRegex.AVOID_SPECIAL_CHARACTERS,
      message = "Special characters are not allowed")
  @Schema(description = "The customer who is the credit party on the invoice(s).")
  private String seller;

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
  @Size(min = 1, max = 34, message = "This field must be between 1 and 34 characters long")
  @Pattern(
      regexp = FieldValidationRegex.INVOICE_NUMBER,
      message = "This field allows numbers and hyphens (-) only")
  @Schema(description = "The invoice number.")
  private String invoiceNumber;

  @NotNull(message = "This field is required.")
  @Pattern(
      regexp = FieldValidationRegex.FORMATTED_DATE,
      message = "Date must be in the format " + DATE_FORMAT)
  @ValidDateFormat(message = "Invalid date", pattern = DATE_FORMAT)
  @Schema(description = "The date the invoice(s) were issued.", format = DATE_FORMAT, type = "date")
  private String issueDate;

  @Valid
  @NotNull(message = "This field is required.")
  @Schema(description = "The total amount of the invoice in the denominated currency.")
  private CurrencyAmountDTO faceValue;

  @Valid
  @NotNull(message = "This field is required.")
  @Schema(description = "Face value amount less any related adjustments.")
  private CurrencyAmountDTO outstandingAmount;

  @NotNull(message = "This field is required.")
  @Pattern(
      regexp = FieldValidationRegex.FORMATTED_DATE,
      message = "Date must be in the format " + DATE_FORMAT)
  @ValidDateFormat(message = "Invalid date", pattern = DATE_FORMAT)
  @Schema(description = "Invoice payment due date.", format = DATE_FORMAT, type = "string")
  private String settlementDate;
}
