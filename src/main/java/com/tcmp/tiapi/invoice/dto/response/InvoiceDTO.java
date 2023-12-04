package com.tcmp.tiapi.invoice.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tcmp.tiapi.shared.dto.response.CurrencyAmountDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceDTO {
  private static final String DATE_FORMAT = "dd-MM-yyyy";

  @Schema(description = "TI identifier.")
  private Long id;

  // Business identifier
  @Schema(description = "Invoice number (unique identifier).")
  private String invoiceNumber;

  @Schema private Long buyerPartyId;

  @Schema private Long createFinanceEventId;

  @Schema private String batchId;

  @Schema private InvoiceCounterPartyDTO buyer;

  @Schema private InvoiceCounterPartyDTO seller;

  @Schema private InvoiceProgramDTO programme;

  @Schema private Long bulkPaymentMasterId;

  @Schema(
      maxLength = 1,
      description =
          "F = Pool based factoring; R = Buyer centric finance; D = Seller centric finance")
  private Character subTypeCategory;

  @Schema(maxLength = 1, description = "B = Buyer centric; S = Seller centric.")
  private Character programType;

  @Schema private Boolean isApproved;

  @Schema(
      maxLength = 1,
      description =
          "O = Outstanding; L = Overdue; P = Paid; D = Inquiry; E = Dishonoured; C = Cancelled")
  private Character status;

  @JsonFormat(pattern = DATE_FORMAT)
  @Schema
  private LocalDate detailsReceivedOn;

  @JsonFormat(pattern = DATE_FORMAT)
  @Schema
  private LocalDate settlementDate;

  @JsonFormat(pattern = DATE_FORMAT)
  @Schema
  private LocalDate issueDate;

  @Schema private Boolean isDisclosed;

  @Schema private Boolean isRecourse;

  @Schema private Boolean isDrawDownEligible;

  @Schema private String preferredCurrencyCode;

  @Schema private Boolean isDeferCharged;

  @Schema(
      maxLength = 1,
      description =
          "A = -; B = Forced ineligible; C = Invoice has duplicate reference; c = Forced eligible (Invoice has duplicate reference); D = Invoice status is overdue; d = Forced eligible (Invoice status is overdue); E = Invoice has invalid issue date; e = Forced eligible (Invoice has invalid issue date); F = Invoice is post-dated; f = Forced eligible (Invoice is post-dated); G = Invoice is stale; g = Forced eligible (Invoice is stale); H = Invoice has invalid seller; h = Forced eligible (Invoice has invalid seller); I = Invoice has invalid buyer; i = Forced eligible (Invoice has invalid buyer); J = Invoice has blocked buyer; j = Forced eligible (Invoice has blocked buyer); K = Invoice has invalid seller buyer relationship; k = Forced eligible (Invoice has invalid seller buyer relationship); L = Invoice has invalid settlement date; l = Forced eligible (Invoice has invalid settlement date); M = Invoice settlement date outside of maximum period; m = Forced eligible (Invoice settlement date outside of maximum period); N = Invoice status is dishonoured; n = Forced eligible (Invoice status is dishonoured); O = Invoice status is cancelled; o = Forced eligible (Invoice status is cancelled); P = Invoice status is under inquiry; p = Forced eligible (Invoice status is under inquiry); Q = Invoice status is paid; q = Forced eligible (Invoice status is paid); R = Invoice has referred buyer; r = Forced eligible (Invoice has referred buyer); S = Invoice is being financed; T = Invoice requires approval; t = Forced eligible (Invoice requires approval); U = Invoice has blocked seller; u = Forced eligible (Invoice has blocked seller); V = Invoice has referred seller; v = Forced eligible (Invoice has referred seller); W = Invoice has invalid received on date; w = Forced eligible (Invoice has invalid received on date); X = Invoice programme or seller buyer limits fully utilised; x = Forced eligible (Invoice programme or seller buyer limits fully utilised); Y = Invoice is already financed; Z = Invoice is booked off; 0 = Gateway transaction refused.")
  private Character eligibilityReasonCode;

  @Schema private CurrencyAmountDTO faceValue;

  @Schema private CurrencyAmountDTO totalPaid;

  @Schema private CurrencyAmountDTO outstanding;

  @Schema private CurrencyAmountDTO advanceAvailable;

  @Schema private CurrencyAmountDTO advanceAvailableEquivalent;

  @Schema private CurrencyAmountDTO discountAdvance;

  @Schema private CurrencyAmountDTO discountDeal;

  @Schema(maxLength = 520, description = "Details notes for customer.")
  private String detailsNotesForCustomer;

  @Schema(maxLength = 370, description = "Invoice security details.")
  private String securityDetails;

  @Schema(maxLength = 148, description = "Invoice tax details.")
  private String taxDetails;
}
