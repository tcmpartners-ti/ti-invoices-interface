package com.tcmp.tiapi.invoice.repository;

import com.tcmp.tiapi.invoice.model.InvoiceMaster;
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
          AND seller.mnemonic = :sellerMnemonic
      """)
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
      AND seller.mnemonic = :sellerMnemonic
  """)
  Optional<BigDecimal> getNotFinancedOutstandingBalanceBySellerMnemonic(
      String sellerMnemonic, @Nullable String buyerMnemonic);
}
