package com.tcmp.tiapi.invoice.repository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.tcmp.tiapi.invoice.model.InvoiceMaster;
import jakarta.persistence.criteria.*;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class InvoiceSpecificationsTest {
  @Test
  void filterBySellerMnemonicAndStatus_itShouldHandleStatus() {
    Root<InvoiceMaster> root = mock();
    var query = mock(CriteriaQuery.class);
    var cb = mock(CriteriaBuilder.class);

    var sellerMnemonic = "1722466420001";
    var status = "O";
    var today = LocalDate.of(2024, 2, 8);

    var specification =
        InvoiceSpecifications.filterBySellerMnemonicAndStatus(sellerMnemonic, status, today);

    List<Predicate> predicates = mock();
    Predicate predicate = mock();
    Path<Object> path = mock();

    when(cb.equal(any(), any())).thenReturn(mock(Predicate.class));
    when(cb.not(any())).thenReturn(predicate);
    when(cb.and(any())).thenReturn(predicate);
    when(root.get(anyString())).thenReturn(path);
    when(root.get(anyString()).get(anyString())).thenReturn(path);
    when(predicates.toArray(any(Predicate[].class))).thenReturn(new Predicate[0]);

    specification.toPredicate(root, query, cb);

    verify(cb).equal(any(), eq(sellerMnemonic));
    verify(cb).equal(any(), eq("O"));
    verify(cb, times(2)).not(any());
  }
}
