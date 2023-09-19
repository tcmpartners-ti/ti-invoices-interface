package com.tcmp.tiapi.invoice.repository;

import com.tcmp.tiapi.invoice.model.InvoiceMaster;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.persistence.criteria.Predicate;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class InvoiceSpecifications {
  public static Specification<InvoiceMaster> filterBySellerIdsAndStatus(
    @Nonnull List<Long> sellerIds,
    @Nullable String status
  ) {
    return (root, query, criteriaBuilder) -> {
      List<Predicate> predicates = new ArrayList<>();

      predicates.add(root.get("sellerId").in(sellerIds));

      if (status != null) {
        predicates.add(criteriaBuilder.equal(root.get("status"), status));
      }

      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
  }
}
