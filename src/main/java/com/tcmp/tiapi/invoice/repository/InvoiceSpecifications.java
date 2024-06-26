package com.tcmp.tiapi.invoice.repository;

import com.tcmp.tiapi.invoice.model.InvoiceMaster;
import com.tcmp.tiapi.invoice.model.ProductMasterStatus;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class InvoiceSpecifications {
  public static Specification<InvoiceMaster> filterBySellerMnemonicAndStatus(
      @Nonnull String sellerMnemonic, @Nullable String status, @Nullable LocalDate today) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();

      predicates.add(cb.equal(root.get("seller").get("mnemonic"), sellerMnemonic));
      predicates.add(
          cb.equal(root.get("productMaster").get("status"), ProductMasterStatus.LIV.name()));

      if (status == null) {
        return cb.and(predicates.toArray(new Predicate[0]));
      }

      // F status is not a native status, it's overridden with O status.
      String dbStatus = status.equals("F") ? "O" : status;
      predicates.add(cb.equal(root.get("status"), dbStatus));

      Predicate invoiceHasBeenFinanced =
          cb.and(
              cb.not(root.get("isDrawDownEligible")),
              cb.isNotNull(root.get("createFinanceEventId")),
              cb.and(
                  cb.isNotNull(root.get("discountDealAmount")),
                  cb.notEqual(root.get("discountDealAmount"), 0)));

      if (status.equals("O")) {
        predicates.add(cb.not(invoiceHasBeenFinanced));
        predicates.add(cb.greaterThan(root.get("settlementDate"), today));
      } else if (status.equals("F")) {
        predicates.add(invoiceHasBeenFinanced);
      }

      query.orderBy(cb.asc(root.get("reference")));

      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }

  public static Specification<InvoiceMaster> filterByBuyerMnemonicAndStatus(
      @Nonnull String buyerMnemonic, @Nullable String status, @Nullable LocalDate today) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();

      predicates.add(cb.equal(root.get("buyer").get("mnemonic"), buyerMnemonic));
      predicates.add(
          cb.equal(root.get("productMaster").get("status"), ProductMasterStatus.LIV.name()));

      if (status == null) {
        return cb.and(predicates.toArray(new Predicate[0]));
      }

      String dbStatus = status.equals("F") ? "O" : status;
      predicates.add(cb.equal(root.get("status"), dbStatus));

      Predicate invoiceHasBeenFinanced =
          cb.and(
              cb.not(root.get("isDrawDownEligible")),
              cb.isNotNull(root.get("createFinanceEventId")),
              cb.and(
                  cb.isNotNull(root.get("discountDealAmount")),
                  cb.notEqual(root.get("discountDealAmount"), 0)));

      if (status.equals("O")) {
        predicates.add(cb.not(invoiceHasBeenFinanced));
        predicates.add(cb.greaterThan(root.get("settlementDate"), today));
      } else if (status.equals("F")) {
        predicates.add(invoiceHasBeenFinanced);
      }

      query.orderBy(cb.asc(root.get("reference")));

      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }
}
