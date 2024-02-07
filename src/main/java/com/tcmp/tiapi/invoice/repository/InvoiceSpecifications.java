package com.tcmp.tiapi.invoice.repository;

import com.tcmp.tiapi.invoice.model.InvoiceMaster;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class InvoiceSpecifications {
  public static Specification<InvoiceMaster> filterBySellerMnemonicAndStatus(
      @Nonnull String sellerMnemonic, @Nullable String status) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();

      predicates.add(cb.equal(root.get("seller").get("mnemonic"), sellerMnemonic));

      if (status != null) {
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
        } else if (status.equals("F")) {
          predicates.add(invoiceHasBeenFinanced);
        }
      }

      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }
}
