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
    return (root, query, criteriaBuilder) -> {
      List<Predicate> predicates = new ArrayList<>();

      predicates.add(criteriaBuilder.equal(root.get("seller").get("mnemonic"), sellerMnemonic));

      if (status != null) {
        predicates.add(criteriaBuilder.equal(root.get("status"), status));
      }

      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
  }
}
