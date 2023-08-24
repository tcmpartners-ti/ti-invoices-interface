package com.tcmp.tiapi.invoice.dto;

import lombok.*;
import org.apache.camel.dataformat.bindy.annotation.CsvRecord;
import org.apache.camel.dataformat.bindy.annotation.DataField;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@CsvRecord(separator = ",")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class InvoiceCreationRowCSV implements Serializable {
  @DataField(pos = 1, name = "CustomerMnemonic")
  private String customerMnemonic;

  @DataField(pos = 2, name = "TheirReference")
  private String theirReference;

  @DataField(pos = 3, name = "Branch")
  private String branch;

  @DataField(pos = 4, name = "BehalfOfBranch")
  private String behalfOfBranch;

  @DataField(pos = 5, name = "AnchorPartyMnemonic")
  private String anchorPartyMnemonic;

  @DataField(pos = 6, name = "ProgrammeId")
  private String programmeId;

  @DataField(pos = 7, name = "SellerId")
  private String sellerId;

  @DataField(pos = 8, name = "BuyerId")
  private String buyerId;

  @DataField(pos = 9, name = "ReceivedOn", pattern = "yyyy-MM-dd")
  private LocalDate receivedOn;

  @DataField(pos = 10, name = "InvoiceNumber")
  private String invoiceNumber;

  @DataField(pos = 11, name = "IssueDate", pattern = "yyyy-MM-dd")
  private LocalDate issueDate;

  @DataField(pos = 12, name = "FaceValueAmount", pattern = "#.##", precision = 2)
  private BigDecimal faceValueAmount;

  @DataField(pos = 13, name = "FaceValueCurrency")
  private String faceValueCurrency;

  @DataField(pos = 14, name = "OutstandingAmount", pattern = "#.##", precision = 2)
  private BigDecimal outstandingAmount;

  @DataField(pos = 15, name = "OutstandingCurrency")
  private String outstandingCurrency;

  @DataField(pos = 16, name = "SettlementDate", pattern = "yyyy-MM-dd")
  private LocalDate settlementDate;
}
