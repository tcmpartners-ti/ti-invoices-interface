package com.tcmp.tiapi.invoice.model;

import com.tcmp.tiapi.customer.model.CounterParty;
import com.tcmp.tiapi.program.model.Program;
import com.tcmp.tiapi.shared.converter.DatabaseBooleanConverter;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.*;

@Entity
@Table(name = "INVMASTER")
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class InvoiceMaster {
  @Id
  @Column(name = "KEY97", nullable = false)
  private Long id;

  @Size(max = 34)
  @Column(name = "INVOIC_REF", length = 34)
  private String reference; // Business identifier

  @Column(name = "INVDATERCD")
  private LocalDate detailsReceivedOn;

  @Column(name = "CUST_PTY")
  private Long customerPartyId;

  @Column(name = "BUYER_PTY")
  private Long buyerPartyId;

  @Size(max = 20)
  @Column(name = "BATCHID", length = 20)
  private String batchId;

  @Size(max = 20)
  @Column(name = "XXXBUYER", length = 20)
  private String xxxbuyer;

  @Column(name = "FACE_AMT", precision = 15)
  private BigDecimal faceValueAmount;

  @Size(max = 3)
  @Column(name = "FACE_CCY", length = 3)
  private String faceValueCurrencyCode;

  @Column(name = "ADJ_AMT", precision = 15)
  private BigDecimal adjustmentAmount;

  @Size(max = 3)
  @Column(name = "ADJ_CCY", length = 3)
  private String adjustmentAmountCurrencyCode;

  @Column(name = "TOTPAYAMT", precision = 15)
  private BigDecimal totalPaidAmount;

  @Size(max = 3)
  @Column(name = "TOTPAYCCY", length = 3)
  private String totalPaidCurrencyCode;

  @Column(name = "OUTS_AMT", precision = 15)
  private BigDecimal outstandingAmount;

  @Size(max = 3)
  @Column(name = "OUTS_CCY", length = 3)
  private String outstandingAmountCurrencyCode;

  @Column(name = "DUEDATE")
  private LocalDate settlementDate;

  @Size(max = 222)
  @Column(name = "GDSDESC", length = 222)
  private String goodsDescription;

  @Size(max = 222)
  @Column(name = "PAY_INSTR", length = 222)
  private String paymentInstructions;

  @Size(max = 370)
  @Column(name = "SEC_DTLS", length = 370)
  private String securityDetails;

  @Column(name = "DISCLOSED")
  @Convert(converter = DatabaseBooleanConverter.class)
  private Boolean isDisclosed;

  @Size(max = 148)
  @Column(name = "TAX_DTLS", length = 148)
  private String taxDetails;

  @Column(name = "RECOURSE")
  @Convert(converter = DatabaseBooleanConverter.class)
  private Boolean isRecourse;

  @Column(name = "STATUS")
  private Character status;

  @Column(name = "ELIGIBLE")
  @Convert(converter = DatabaseBooleanConverter.class)
  private Boolean isDrawDownEligible;

  @Column(name = "OVR_ELIG")
  private Character overrideEligibilityCode;

  @Size(max = 4)
  @Column(name = "ADJ_DIR", length = 4)
  private String adjDir;

  @Column(name = "FACTOR_KEY")
  private Long factoringMasterId;

  @Size(max = 3)
  @Column(name = "PFR_CCY", length = 3)
  private String preferredCurrencyCode;

  @Column(name = "DEFER_CHG")
  @Convert(converter = DatabaseBooleanConverter.class)
  private Boolean isDeferCharged;

  @Column(name = "ELIG_DTLS")
  private Character eligibilityReasonCode;

  @Column(name = "FINCE_EV")
  private Long createFinanceEventId;

  @Column(name = "AVAIL_AMT", precision = 15)
  private BigDecimal advanceAvailableAmount;

  @Size(max = 3)
  @Column(name = "AVAIL_CCY", length = 3)
  private String advanceAvailableCurrencyCode;

  @Column(name = "CREDN_AMT", precision = 15)
  private BigDecimal creditNoteAmount;

  @Size(max = 3)
  @Column(name = "CREDN_CCY", length = 3)
  private String creditNoteAmountCurrencyCode;

  @Column(name = "EQUIV_AMT", precision = 15)
  private BigDecimal advanceAvailableEquivalentAmount;

  @Size(max = 3)
  @Column(name = "EQUIV_CCY", length = 3)
  private String advanceAvailableEquivalentCurrencyCode;

  @Size(max = 520)
  @Column(name = "ADV_INSTR", length = 520)
  private String detailsAdvanceInstructions;

  @Size(max = 520)
  @Column(name = "CUST_NOTES", length = 520)
  private String detailsNotesForCustomer;

  @Column(name = "DISC_AMT", precision = 15)
  private BigDecimal discountAdvanceAmount;

  @Size(max = 3)
  @Column(name = "DISC_CCY", length = 3)
  private String discountAdvanceAmountCurrencyCode;

  @Column(name = "DEAL_AMT", precision = 15)
  private BigDecimal discountDealAmount;

  @Size(max = 3)
  @Column(name = "DEAL_CCY", length = 3)
  private String discountDealAmountCurrencyCode;

  @Column(name = "APPROVED")
  @Convert(converter = DatabaseBooleanConverter.class)
  private Boolean isApproved;

  @Size(max = 35)
  @Column(name = "VENDORCODE", length = 35)
  private String vendorId;

  @Column(name = "BUYER")
  private Long buyerId;

  @Column(name = "SELLER")
  private Long sellerId;

  @Column(name = "PROGRAMME")
  private Long programmeId;

  @Column(name = "INVOICEFOR")
  private Character subTypeCategory;

  @Column(name = "PROG_TYPE")
  private Character programType;

  @Column(name = "IBPMASTER")
  private Long bulkPaymentMasterId;

  @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @JoinColumn(name = "KEY97", referencedColumnName = "KEY97", insertable = false, updatable = false)
  private ProductMaster productMaster; // Parent table

  @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @JoinColumn(name = "KEY97", referencedColumnName = "MASTER", insertable = false, updatable = false)
  private ProductMasterExtension productMasterExtension;

  @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @JoinColumn(name = "BUYER", referencedColumnName = "KEY97", insertable = false, updatable = false)
  private CounterParty buyer;

  @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @JoinColumn(name = "SELLER", referencedColumnName = "KEY97", insertable = false, updatable = false)
  private CounterParty seller;

  @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @JoinColumn(name = "PROGRAMME", referencedColumnName = "KEY97", insertable = false, updatable = false)
  private Program program;
}
