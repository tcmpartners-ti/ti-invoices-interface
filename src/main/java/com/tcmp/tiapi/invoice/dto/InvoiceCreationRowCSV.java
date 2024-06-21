package com.tcmp.tiapi.invoice.dto;

import com.opencsv.bean.CsvBindByName;
import java.io.Serializable;
import lombok.*;
import org.apache.camel.dataformat.bindy.annotation.CsvRecord;

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

  @CsvBindByName(column = "Index")
  private String index;

  @CsvBindByName(column = "Customer", required = true)
  private String customer;

  @CsvBindByName(column = "ReferenceId", required = true)
  private String theirReference;

  @CsvBindByName(column = "BehalfOfBranch", required = true)
  private String behalfOfBranch;

  @CsvBindByName(column = "AnchorParty", required = true)
  private String anchorParty;

  @CsvBindByName(column = "ProgrammeId", required = true)
  private String programme;

  @CsvBindByName(column = "SellerDocumentNumber", required = true)
  private String seller;

  @CsvBindByName(column = "BuyerDocumentNumber", required = true)
  private String buyer;

  @CsvBindByName(column = "BillNumber", required = true)
  private String invoiceNumber;

  @CsvBindByName(column = "IssueDate", required = true)
  private String issueDate;

  @CsvBindByName(column = "FaceValueAmount", required = true)
  private String faceValueAmount;

  @CsvBindByName(column = "FaceValueCurrency", required = true)
  private String faceValueCurrency;

  @CsvBindByName(column = "OutstandingAmount", required = true)
  private String outstandingAmount;

  @CsvBindByName(column = "OutstandingCurrency", required = true)
  private String outstandingCurrency;

  @CsvBindByName(column = "SettlementDate", required = true)
  private String settlementDate;

  @CsvBindByName(column = "CurrentAccountAnchor", required = true)
  private String anchorAccount;
}
