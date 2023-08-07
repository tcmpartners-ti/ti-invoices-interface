package com.tcmp.tiapi.invoice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "INITINVOIC")
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class InvoiceCreationEvent {
    @Id
    @Column(name = "KEY97", nullable = false)
    private Long id;

    @Column(name = "BUYER")
    private Long buyerKey;

    @Column(name = "SELLER")
    private Long sellerKey;

    @Column(name = "PROGRAMME")
    private Long programmeKey;

    @Column(name = "DATE_RECVD")
    private LocalDate detailsReceivedOn;

    @Column(name = "CUST_PTY")
    private Long customerPartyDetailsKey;

    @Column(name = "BUYER_PTY")
    private Long buyerPartyDetailsKey;

    @Nationalized
    @Column(name = "BATCHID", length = 20)
    private String batchId;

    @Nationalized
    @Column(name = "INVOIC_REF", length = 34)
    private String invoiceReference;

    @Nationalized
    @Column(name = "XXXBUYER", length = 20)
    private String creditNotePartiesBuyer;

    @Column(name = "FACE_AMT", precision = 15)
    private BigDecimal faceValueAmount;

    @Nationalized
    @Column(name = "FACE_CCY", length = 3)
    private String faceValueCurrency;

    @Column(name = "ADJ_AMT", precision = 15)
    private BigDecimal adjustmentAmount;

    @Nationalized
    @Column(name = "ADJ_CCY", length = 3)
    private String adjustmentCurrency;

    @Column(name = "TOTPAYAMT", precision = 15)
    private BigDecimal totalPaidAmount;

    @Nationalized
    @Column(name = "TOTPAYCCY", length = 3)
    private String totalPaidCurrency;

    @Column(name = "OUTS_AMT", precision = 15)
    private BigDecimal outstandingAmount;

    @Nationalized
    @Column(name = "OUTS_CCY", length = 3)
    private String outstandingCurrency;

    @Column(name = "DUEDATE")
    private LocalDate dueDate;

    @Nationalized
    @Column(name = "GDSDESC", length = 222)
    private String goodsDescription;

    @Nationalized
    @Column(name = "PAY_INSTR", length = 222)
    private String paymentsInstruction;

    @Nationalized
    @Column(name = "SEC_DTLS", length = 370)
    private String securityDetails;

    @Nationalized
    @Column(name = "DISCLOSED", length = 1)
    private String isDisclosed;

    @Nationalized
    @Column(name = "TAX_DTLS", length = 148)
    private String taxDetails;

    @Nationalized
    @Column(name = "RECOURSE", length = 1)
    private String isRecourse;

    @Nationalized
    @Column(name = "STATUS", length = 1)
    private String status;

    @Nationalized
    @Column(name = "ELIGIBLE", length = 1)
    private String isEligible;

    @Nationalized
    @Column(name = "OVR_ELIG", length = 1)
    private String overrideEligibility;

    @Nationalized
    @Column(name = "ADJ_DIR", length = 4)
    private String adjustmentsDirection;

    @Column(name = "FACTOR_KEY")
    private Long factoringKey;

    @Nationalized
    @Column(name = "PFR_CCY", length = 3)
    private String preferredCurrency;

    @Nationalized
    @Column(name = "DEFER_CHG", length = 1)
    private String isChargedByDefer;

    @Nationalized
    @Column(name = "ELIG_DTLS", length = 1)
    private String eligibilityReason;

    @Column(name = "FINCE_EV")
    private Long createFinanceEventKey;

    @Column(name = "AVAIL_AMT", precision = 15)
    private BigDecimal advanceAvailableAmount;

    @Nationalized
    @Column(name = "AVAIL_CCY", length = 3)
    private String advanceAvailableCurrency;

    @Column(name = "CREDN_AMT", precision = 15)
    private BigDecimal creditNoteAmount;

    @Nationalized
    @Column(name = "CREDN_CCY", length = 3)
    private String creditNoteCurrency;

    @Column(name = "EQUIV_AMT", precision = 15)
    private BigDecimal equivalentAmount;

    @Nationalized
    @Column(name = "EQUIV_CCY", length = 3)
    private String equivalentCurrency;

    @Nationalized
    @Column(name = "ADV_INSTR", length = 520)
    private String advanceInstructions;

    @Nationalized
    @Column(name = "CUST_NOTES", length = 520)
    private String notesForCustomer;

    @Column(name = "DISC_AMT", precision = 15)
    private BigDecimal discountAdvanceAmount;

    @Nationalized
    @Column(name = "DISC_CCY", length = 3)
    private String discountAdvanceCurrency;

    @Column(name = "DEAL_AMT", precision = 15)
    private BigDecimal discountDealAmount;

    @Nationalized
    @Column(name = "DEAL_CCY", length = 3)
    private String discountDealCurrency;

    @Nationalized
    @Column(name = "APPROVED", length = 1)
    private String isApproved;

    @Nationalized
    @Column(name = "VENDORCODE", length = 35)
    private String vendorCode;

    @Nationalized
    @Column(name = "INVOICEFOR", length = 1)
    private String subtypeCategory;

    @Nationalized
    @Column(name = "PROG_TYPE", length = 1)
    private String detailsType;
}
