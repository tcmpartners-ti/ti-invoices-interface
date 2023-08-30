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

  @Schema(name = "batchId")
  private String batchId;

  @Schema(name = "buyer")
  private InvoiceCounterPartyDTO buyer;

  @Schema(name = "seller")
  private InvoiceCounterPartyDTO seller;

  @Schema(name = "programme")
  private InvoiceProgramDTO program;

  @Schema(name = "bulkPaymentMasterId")
  private Long bulkPaymentMasterId;

  @Schema(name = "subTypeCategory", maxLength = 1, description = "F = Pool based factoring; R = Buyer centric finance; D = Seller centric finance")
  private Character subTypeCategory;

  @Schema(name = "programType", maxLength = 1, description = "B = Buyer centric; S = Seller centric.")
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

  @Schema(name = "preferredCurrencyCode")
  private String preferredCurrencyCode;

  @Schema(name = "isDeferCharged")
  private Boolean isDeferCharged;

  @Schema(name = "eligibilityReasonCode", maxLength = 1, description = "A = -; B = Forced ineligible; C = Invoice has duplicate reference; c = Forced eligible (Invoice has duplicate reference); D = Invoice status is overdue; d = Forced eligible (Invoice status is overdue); E = Invoice has invalid issue date; e = Forced eligible (Invoice has invalid issue date); F = Invoice is post-dated; f = Forced eligible (Invoice is post-dated); G = Invoice is stale; g = Forced eligible (Invoice is stale); H = Invoice has invalid seller; h = Forced eligible (Invoice has invalid seller); I = Invoice has invalid buyer; i = Forced eligible (Invoice has invalid buyer); J = Invoice has blocked buyer; j = Forced eligible (Invoice has blocked buyer); K = Invoice has invalid seller buyer relationship; k = Forced eligible (Invoice has invalid seller buyer relationship); L = Invoice has invalid settlement date; l = Forced eligible (Invoice has invalid settlement date); M = Invoice settlement date outside of maximum period; m = Forced eligible (Invoice settlement date outside of maximum period); N = Invoice status is dishonoured; n = Forced eligible (Invoice status is dishonoured); O = Invoice status is cancelled; o = Forced eligible (Invoice status is cancelled); P = Invoice status is under inquiry; p = Forced eligible (Invoice status is under inquiry); Q = Invoice status is paid; q = Forced eligible (Invoice status is paid); R = Invoice has referred buyer; r = Forced eligible (Invoice has referred buyer); S = Invoice is being financed; T = Invoice requires approval; t = Forced eligible (Invoice requires approval); U = Invoice has blocked seller; u = Forced eligible (Invoice has blocked seller); V = Invoice has referred seller; v = Forced eligible (Invoice has referred seller); W = Invoice has invalid received on date; w = Forced eligible (Invoice has invalid received on date); X = Invoice programme or seller buyer limits fully utilised; x = Forced eligible (Invoice programme or seller buyer limits fully utilised); Y = Invoice is already financed; Z = Invoice is booked off; 0 = Gateway transaction refused.")
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

  @Schema(name = "detailsNotesForCustomer", maxLength = 520, description = "Details notes for customer.")
  private String detailsNotesForCustomer;

  @Schema(name = "securityDetails", maxLength = 370, description = "Invoice security details.")
  private String securityDetails;

  @Schema(name = "taxDetails", maxLength = 148, description = "Invoice tax details.")
  private String taxDetails;
}
