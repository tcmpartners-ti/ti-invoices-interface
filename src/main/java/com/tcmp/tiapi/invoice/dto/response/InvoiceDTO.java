package com.tcmp.tiapi.invoice.dto.response;

import com.tcmp.tiapi.shared.dto.response.CurrencyAmountDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class InvoiceDTO {

  @Schema(name = "id", description = "TI identifier.")
  private Long id;

  // Business identifier
  @Schema(name = "invoiceNumber", description = "Invoice number (unique identifier).")
  private String invoiceNumber;


  @Schema(name = "buyerPartyId")
  private Long buyerPartyId;


  @Schema(name = "createFinanceEventId")
  private Long createFinanceEventId;

  // Todo: check if neeeded
  @Schema(name = "batchId")
  private String batchId;


  @Schema(name = "buyerId")
  private Long buyerId;


  @Schema(name = "sellerId")
  private Long sellerId;


  @Schema(name = "programmeId")
  private Long programmeId;


  @Schema(name = "bulkPaymentMasterId")
  private Long bulkPaymentMasterId;


  @Schema(name = "subTypeCategory")
  private Character subTypeCategory;


  @Schema(name = "programType")
  private Character programType;


  @Schema(name = "isApproved")
  private Boolean isApproved;


  @Schema(name = "status")
  private Character status;


  @Schema(name = "detailsReceivedOn")
  private LocalDate detailsReceivedOn;


  @Schema(name = "settlementDate")
  private LocalDate settlementDate;


  @Schema(name = "isDisclosed")
  private Boolean isDisclosed;


  @Schema(name = "isRecourse")
  private Boolean isRecourse;


  @Schema(name = "isDrawDownEligible")
  private Boolean isDrawDownEligible;

  // Todo: check if needed

  @Schema(name = "preferredCurrencyCode")
  private String preferredCurrencyCode;


  @Schema(name = "isDeferCharged")
  private Boolean isDeferCharged;


  @Schema(name = "eligibilityReasonCode")
  private Character eligibilityReasonCode;


  @Schema(name = "faceValue")
  private CurrencyAmountDTO faceValue;


  @Schema(name = "totalPaid")
  private CurrencyAmountDTO totalPaid;


  @Schema(name = "outstanding")
  private CurrencyAmountDTO outstanding;


  @Schema(name = "advanceAvailable")
  private CurrencyAmountDTO advanceAvailable;


  @Schema(name = "advanceAvailableEquivalent")
  private CurrencyAmountDTO advanceAvailableEquivalent;


  @Schema(name = "discountAdvance")
  private CurrencyAmountDTO discountAdvance;


  @Schema(name = "discountDeal")
  private CurrencyAmountDTO discountDeal;


  @Schema(name = "detailsNotesForCustomer")
  private String detailsNotesForCustomer;

  // Todo: check if needed

  @Schema(name = "securityDetails")
  private String securityDetails;


  @Schema(name = "taxDetails")
  private String taxDetails;
}
