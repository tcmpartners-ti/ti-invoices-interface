package com.tcmp.tiapi.invoice.dto;

import lombok.*;
import org.apache.camel.dataformat.bindy.annotation.CsvRecord;
import org.apache.camel.dataformat.bindy.annotation.DataField;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@CsvRecord(separator = ";")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class InvoiceCreationRowCSV implements Serializable {
  private static final String NUMBER_FORMAT = "#.##";
  private static final String DATE_FORMAT = "dd-MM-yyyy";

  @DataField(pos = 1, name = "CustomerMnemonic")
  private String customerMnemonic;

  @DataField(pos = 2, name = "TheirReference")
  private String theirReference;

  @DataField(pos = 3, name = "BehalfOfBranch")
  private String behalfOfBranch;

  @DataField(pos = 4, name = "AnchorPartyMnemonic")
  private String anchorPartyMnemonic;

  @DataField(pos = 5, name = "ProgrammeId")
  private String programmeId;

  @DataField(pos = 6, name = "SellerId")
  private String sellerId;

  @DataField(pos = 7, name = "BuyerId")
  private String buyerId;

  @DataField(pos = 8, name = "InvoiceNumber")
  private String invoiceNumber;

  @DataField(pos = 9, name = "IssueDate", pattern = DATE_FORMAT)
  private LocalDate issueDate;

  @DataField(pos = 10, name = "FaceValueAmount", pattern = NUMBER_FORMAT, precision = 2)
  private BigDecimal faceValueAmount;

  @DataField(pos = 11, name = "FaceValueCurrency")
  private String faceValueCurrency;

  @DataField(pos = 12, name = "OutstandingAmount", pattern = NUMBER_FORMAT, precision = 2)
  private BigDecimal outstandingAmount;

  @DataField(pos = 13, name = "OutstandingCurrency")
  private String outstandingCurrency;

  @DataField(pos = 14, name = "SettlementDate", pattern = DATE_FORMAT)
  private LocalDate settlementDate;
}
