package com.tcmp.tiapi.invoice.dto;

import com.opencsv.bean.CsvBindByPosition;
import com.tcmp.tiapi.shared.FieldValidationRegex;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.apache.camel.dataformat.bindy.annotation.CsvRecord;
import org.apache.camel.dataformat.bindy.annotation.DataField;

import java.io.Serializable;

@CsvRecord(separator = ",")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class InvoiceCreationRowCSV implements Serializable {
  private static final String NUMBER_FORMAT = "#.##";
  private static final String DATE_FORMAT = "dd-MM-yyyy";

  @NotNull(message = "This field is required.")
  @Size(min = 1, max = 20, message = "This field must be between 1 and 20 characters.")
  @Pattern(regexp = FieldValidationRegex.ONLY_NUMERIC_VALUES, message = "Only numeric values are allowed.")
  @DataField(pos = 1, name = "CustomerMnemonic")
  @CsvBindByPosition(position = 0)
  private String customer;

  @NotNull(message = "This field is required.")
  @Size(min = 1, max = 34, message = "This field must be between 1 and 34 characters.")
  @DataField(pos = 2, name = "TheirReference")
  @CsvBindByPosition(position = 1)
  private String theirReference;

  @NotNull(message = "This field is required.")
  @NotBlank(message = "This field is required.")
  @Size(min = 1, max = 8, message = "This field must be between 1 and 8 characters.")
  @DataField(pos = 3, name = "BehalfOfBranch")
  @CsvBindByPosition(position = 2)
  private String behalfOfBranch;

  @NotNull(message = "This field is required.")
  @Size(min = 1, max = 20, message = "This field must be between 1 and 20 characters.")
  @Pattern(regexp = FieldValidationRegex.ONLY_NUMERIC_VALUES, message = "Only numeric values are allowed.")
  @DataField(pos = 4, name = "AnchorPartyMnemonic")
  @CsvBindByPosition(position = 3)
  private String anchorParty;

  @NotNull(message = "This field is required.")
  @Size(min = 1, max = 35, message = "This field must be between 1 and 35 characters.")
  @DataField(pos = 5, name = "ProgrammeId")
  @CsvBindByPosition(position = 4)
  private String programme;

  @NotNull(message = "This field is required.")
  @Size(min = 1, max = 20, message = "This field must be between 1 and 20 characters.")
  @Pattern(regexp = FieldValidationRegex.ONLY_NUMERIC_VALUES, message = "Only numeric values are allowed.")
  @DataField(pos = 6, name = "SellerId")
  @CsvBindByPosition(position = 5)
  private String seller;

  @NotNull(message = "This field is required.")
  @Size(min = 1, max = 20, message = "Debited party id must be between 1 and 20 characters long.")
  @Pattern(regexp = FieldValidationRegex.ONLY_NUMERIC_VALUES, message = "Only numeric values are allowed.")
  @DataField(pos = 7, name = "BuyerId")
  @CsvBindByPosition(position = 6)
  private String buyer;

  @NotNull(message = "This field is required.")
  @Size(min = 1, max = 34, message = "Invoice number must be between 1 and 34 characters.")
  @DataField(pos = 8, name = "InvoiceNumber")
  @CsvBindByPosition(position = 7)
  private String invoiceNumber;

  @NotNull(message = "This field is required.")
  @Pattern(regexp = FieldValidationRegex.FORMATTED_DATE, message = "Issue date must be in format dd-MM-yyyy.")
  @DataField(pos = 9, name = "IssueDate", pattern = DATE_FORMAT)
  @CsvBindByPosition(position = 8)
  private String issueDate;

  @NotNull(message = "This field is required.")
  @Pattern(regexp = FieldValidationRegex.NUMBER_WITH_DECIMALS, message = "This field must be in format ##.##")
  @DataField(pos = 10, name = "FaceValueAmount", pattern = NUMBER_FORMAT, precision = 2)
  @CsvBindByPosition(position = 9)
  private String faceValueAmount;

  @NotNull(message = "This field is required.")
  @Size(min = 1, max = 3, message = "Face value currency code must be between 1 and 3 characters.")
  @DataField(pos = 11, name = "FaceValueCurrency")
  @CsvBindByPosition(position = 10)
  private String faceValueCurrency;

  @NotNull(message = "This field is required.")
  @DataField(pos = 12, name = "OutstandingAmount", pattern = NUMBER_FORMAT, precision = 2)
  @Pattern(regexp = FieldValidationRegex.NUMBER_WITH_DECIMALS, message = "This field must be in format ##.##")
  @CsvBindByPosition(position = 11)
  private String outstandingAmount;

  @NotNull(message = "This field is required.")
  @Size(min = 1, max = 3, message = "Outstanding currency code must be between 1 and 3 characters.")
  @DataField(pos = 13, name = "OutstandingCurrency")
  @CsvBindByPosition(position = 12)
  private String outstandingCurrency;

  @NotNull(message = "This field is required.")
  @Pattern(regexp = FieldValidationRegex.FORMATTED_DATE, message = "Settlement date must be in format dd-MM-yyyy.")
  @DataField(pos = 14, name = "SettlementDate", pattern = DATE_FORMAT)
  @CsvBindByPosition(position = 13)
  private String settlementDate;

  @NotNull(message = "This field is required.")
  @Size(min = 1, max = 10, message = "This field must be between 1 and 10 character(s).")
  @Pattern(regexp = FieldValidationRegex.ONLY_NUMERIC_VALUES, message = "Only numeric values are allowed.")
  @DataField(pos = 15, name = "AnchorCurrentAccount")
  @CsvBindByPosition(position = 14)
  private String anchorCurrentAccount;
}
