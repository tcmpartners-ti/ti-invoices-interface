package com.tcmp.tiapi.invoice.repository;

import com.tcmp.tiapi.invoice.model.InvoiceMaster;
import com.tcmp.tiapi.invoice.model.InvoiceToCollectReport;
import com.tcmp.tiapi.invoice.model.InvoiceToPayReport;
import com.tcmp.tiapi.invoice.model.ProductMasterStatus;
import jakarta.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceRepository extends JpaRepository<InvoiceMaster, Long> {
  Optional<InvoiceMaster> findByProductMasterMasterReference(String masterReference);

  Optional<InvoiceMaster> findByProgramIdAndSellerMnemonicAndReference(
      String programId, String sellerMnemonic, String invoiceReference);

  Optional<InvoiceMaster> findByProgramIdAndSellerMnemonicAndReferenceAndProductMasterIsActive(
      String programId, String sellerMnemonic, String invoiceReference, boolean isActive);

  Page<InvoiceMaster> findAll(Specification<InvoiceMaster> spec, Pageable pageable);

  @Query(
      """
        SELECT
          SUM(invoice.outstandingAmount - invoice.discountDealAmount)
        FROM
          InvoiceMaster invoice
        LEFT JOIN CounterParty buyer ON
          buyer.id = invoice.buyer.id
        LEFT JOIN CounterParty seller ON
          seller.id = invoice.seller.id
        LEFT JOIN ProductMaster master ON
          master.id = invoice.id
        WHERE
          invoice.status = 'O'
          AND master.status = 'LIV'
          AND (
              invoice.isDrawDownEligible = false
              AND invoice.createFinanceEventId IS NOT NULL
              AND ( invoice.discountDealAmount IS NOT NULL
                  AND invoice.discountDealAmount != 0 )
          )
          AND (:buyerMnemonic IS NULL OR buyer.mnemonic = :buyerMnemonic)
          AND seller.mnemonic = :sellerMnemonic""")
  Optional<BigDecimal> getFinancedOutstandingBalanceBySellerMnemonic(
      String sellerMnemonic, @Nullable String buyerMnemonic);

  @Query(
      """
    SELECT
      SUM(invoice.outstandingAmount)
    FROM
      InvoiceMaster invoice
    LEFT JOIN CounterParty buyer ON
      buyer.id = invoice.buyer.id
    LEFT JOIN CounterParty seller ON
      seller.id = invoice.seller.id
    LEFT JOIN ProductMaster master ON
      master.id = invoice.id
    WHERE
      invoice.status = 'O'
      AND master.status = 'LIV'
      AND NOT (
          invoice.isDrawDownEligible = false
          AND invoice.createFinanceEventId IS NOT NULL
          AND ( invoice.discountDealAmount IS NOT NULL
              AND invoice.discountDealAmount != 0 )
      )
      AND (:buyerMnemonic IS NULL OR buyer.mnemonic = :buyerMnemonic)
      AND seller.mnemonic = :sellerMnemonic""")
  Optional<BigDecimal> getNotFinancedOutstandingBalanceBySellerMnemonic(
      String sellerMnemonic, @Nullable String buyerMnemonic);

  boolean existsBySellerMnemonicAndBuyerMnemonicAndStatusAndProductMasterStatus(
      String sellerMnemonic,
      String buyerMnemonic,
      Character invoiceStatus,
      ProductMasterStatus masterStatus);

  // Downloadable Reports
  @Query(
      value =
          """
    SELECT
      buyer.CUSTOMER AS buyerMnemonic,
      seller.CUSTOMER AS sellerMnemonic,
      invoice.INVOIC_REF invoiceReference,
      invoice.DUEDATE AS invoiceDueDate,
      (invoice.FACE_AMT / 100) AS invoiceFaceAmount,
      invoice.STATUS AS invoiceStatus,
      buyer.CPARTYNAME AS buyerName,
      seller.CPARTYNAME AS sellerName,
      program.ID AS programmeId,
      (finance.FINCE_AMT / 100) AS financeAmount,
      extra.BGAFINTS AS buyerInterests,
      extra.GAFINTRT AS buyerInterestsRate,
      extra.BSOLCAMT AS buyerSolcaAmount,
      invoice.INVDATERCD AS invoiceDateReceived,
      extraPro.EXFINDAY AS programExtraFinancingDays,
      extra.GAFOPEID AS gafOperationId
    FROM
      INVMASTER invoice
    JOIN SCFCPARTY buyer ON
      buyer.KEY97 = invoice.BUYER
    JOIN SCFCPARTY seller ON
      seller.KEY97 = invoice.SELLER
    JOIN SCFPROGRAM program ON
      program.KEY97 = invoice.PROGRAMME
    JOIN EXTMASTER extra ON
      invoice.KEY97 = extra.MASTER
    JOIN SCFMAP relationship ON
      relationship.PARTY = buyer.KEY97
      AND relationship.CPARTY = seller.KEY97
    JOIN EXTPROGRAMME extraPro ON
      program.ID = extraPro.PID
    JOIN MASTER master ON
      invoice.KEY97 = master.KEY97
      AND master.STATUS = 'LIV'
    LEFT JOIN INTERE59 intSche ON
      relationship.KEY97 = intSche.SCFMAP
      AND intSche.OBSOLETE = 'N'
    LEFT JOIN INT_TIER intTier ON
      intSche.KEY29 = intTier.owner
    LEFT JOIN FNCEMASTER finance ON
      finance.FINCEEVKEY = invoice.FINCE_EV
    WHERE
      buyer.CUSTOMER = :buyerMnemonic""",
      nativeQuery = true)
  Page<InvoiceToPayReport> findInvoiceToPayByBuyerMnemonic(String buyerMnemonic, Pageable page);

  @Query(
      value =
          """
              SELECT
                buyer.CUSTOMER AS buyerMnemonic,
                seller.CUSTOMER AS sellerMnemonic,
                invoice.INVOIC_REF invoiceReference,
                invoice.DUEDATE AS invoiceDueDate,
                (invoice.FACE_AMT / 100) AS invoiceFaceAmount,
                invoice.STATUS AS invoiceStatus,
                buyer.CPARTYNAME AS buyerName,
                seller.CPARTYNAME AS sellerName,
                program.ID AS programmeId,
                (finance.FINCE_AMT / 100) AS financeAmount,
                extra.SGAFINTS AS sellerInterests,
                extra.GAFINTRT AS buyerInterestsRate,
                extra.SSOLCAMT AS sellerSolcaAmount,
                invoice.INVDATERCD AS invoiceDateReceived,
                finance.EFFECTIVE AS financeEffectiveDate,
                finance.KEY97 AS financeEventId
              FROM
                INVMASTER invoice
              JOIN SCFCPARTY buyer ON
                buyer.KEY97 = invoice.BUYER
              JOIN SCFCPARTY seller ON
                seller.KEY97 = invoice.SELLER
              JOIN SCFPROGRAM program ON
                program.KEY97 = invoice.PROGRAMME
              JOIN EXTMASTER extra ON
                invoice.KEY97 = extra.MASTER
              JOIN SCFMAP relationship ON
                relationship.PARTY = buyer.KEY97
                  AND relationship.CPARTY = seller.KEY97
              JOIN EXTPROGRAMME extraPro ON
                program.ID = extraPro.PID
              JOIN MASTER master ON
                invoice.KEY97 = master.KEY97
                  AND master.STATUS = 'LIV'
              LEFT JOIN FNCEMASTER finance ON
                finance.FINCEEVKEY = invoice.FINCE_EV
              WHERE
                seller.CUSTOMER = :sellerMnemonic""",
      nativeQuery = true)
  Page<InvoiceToCollectReport> findInvoiceToCollectBySellerMnemonic(
      String sellerMnemonic, Pageable page);
}
