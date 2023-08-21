package com.tcmp.tiapi.invoice.dto.response;

import com.tcmp.tiapi.shared.dto.response.CurrencyAmountDTO;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class InvoiceDTO {

  private Long id;

  // Business identifier
  private String reference;

  private Long buyerPartyId;

  private Long createFinanceEventId;

  // Todo: check if neeeded
  private String batchId;

  private Long buyerId;

  private Long sellerId;

  private Long programmeId;

  private Long bulkPaymentMasterId;

  private Character subTypeCategory;

  private Character programType;

  private Boolean isApproved;

  private Character status;

  private LocalDate detailsReceivedOn;

  private LocalDate settlementDate;

  private Boolean isDisclosed;

  private Boolean isRecourse;

  private Boolean isDrawDownEligible;

  // Todo: check if needed
  private String preferredCurrencyCode;

  private Boolean isDeferCharged;

  private Character eligibilityReasonCode;

  private CurrencyAmountDTO faceValue;

  private CurrencyAmountDTO totalPaid;

  private CurrencyAmountDTO outstanding;

  private CurrencyAmountDTO advanceAvailable;

  private CurrencyAmountDTO advanceAvailableEquivalent;

  private CurrencyAmountDTO discountAdvance;

  private CurrencyAmountDTO discountDeal;

  private String detailsNotesForCustomer;

  // Todo: check if needed
  private String securityDetails;

  private String taxDetails;
}
