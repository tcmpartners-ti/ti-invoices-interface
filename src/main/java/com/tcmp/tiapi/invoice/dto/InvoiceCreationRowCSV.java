package com.tcmp.tiapi.invoice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.apache.camel.dataformat.bindy.annotation.CsvRecord;
import org.apache.camel.dataformat.bindy.annotation.DataField;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

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
  private static final String DATE_FORMAT_REGEX = "^(0[1-9]|[12][0-9]|3[01])-(0[1-9]|1[0-2])-(\\d{4})$";

  @Size(min = 1, max = 20, message = "Customer must be between 1 and 20 characters.")
  @DataField(pos = 1, name = "CustomerMnemonic")
  private String customerMnemonic;

  @Size(min = 1, max = 34, message = "Their reference must be between 1 and 34 characters.")
  @DataField(pos = 2, name = "TheirReference")
  private String theirReference;

  @Size(min = 1, max = 8, message = "Behalf of Branch must be between 1 and 8 characters.")
  @DataField(pos = 3, name = "BehalfOfBranch")
  private String behalfOfBranch;

  @DataField(pos = 4, name = "AnchorPartyMnemonic")
  private String anchorPartyMnemonic;

  @NotNull(message = "Programme identifier is required.")
  @Size(max = 35, message = "Program identifier max length is 35 characters.")
  @DataField(pos = 5, name = "ProgrammeId")
  private String programmeId;

  @Size(min = 1, max = 20, message = "Seller mnemonic must be between 1 and 20 characters.")
  @DataField(pos = 6, name = "SellerId")
  private String sellerId;

  @Size(min = 1, max = 20, message = "Buyer mnemonic must be between 1 and 20 characters.")
  @DataField(pos = 7, name = "BuyerId")
  private String buyerId;

  @Size(min = 1, max = 34, message = "Invoice number must be between 1 and 34 characters.")
  @DataField(pos = 8, name = "InvoiceNumber")
  private String invoiceNumber;

  @Pattern(regexp = DATE_FORMAT_REGEX, message = "Issue date must be in format dd-MM-yyyy.")
  @DataField(pos = 9, name = "IssueDate", pattern = DATE_FORMAT)
  private LocalDate issueDate;

  @DataField(pos = 10, name = "FaceValueAmount", pattern = NUMBER_FORMAT, precision = 2)
  private BigDecimal faceValueAmount;

  @Size(min = 1, max = 3, message = "Face value currency code must be between 1 and 3 characters.")
  @DataField(pos = 11, name = "FaceValueCurrency")
  private String faceValueCurrency;

  @DataField(pos = 12, name = "OutstandingAmount", pattern = NUMBER_FORMAT, precision = 2)
  private BigDecimal outstandingAmount;

  @Size(min = 1, max = 3, message = "Outstanding currency code must be between 1 and 3 characters.")
  @DataField(pos = 13, name = "OutstandingCurrency")
  private String outstandingCurrency;

  @Pattern(regexp = DATE_FORMAT_REGEX, message = "Settlement date must be in format dd-MM-yyyy.")
  @DataField(pos = 14, name = "SettlementDate", pattern = DATE_FORMAT)
  private LocalDate settlementDate;
}
