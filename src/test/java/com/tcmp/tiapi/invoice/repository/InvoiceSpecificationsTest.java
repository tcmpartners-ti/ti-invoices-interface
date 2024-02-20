package com.tcmp.tiapi.invoice.repository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.tcmp.tiapi.invoice.model.InvoiceMaster;
import jakarta.persistence.criteria.*;
import java.util.List;
import org.junit.jupiter.api.Test;

class InvoiceSpecificationsTest {
  @Test
  void filterBySellerMnemonicAndStatus_itShouldHandleStatus() {
    Root<InvoiceMaster> root = mock();
    CriteriaQuery<?> query = mock(CriteriaQuery.class);
    CriteriaBuilder cb = mock(CriteriaBuilder.class);

    var sellerMnemonic = "1722466420001";
    var status = "O";

    var specification =
        InvoiceSpecifications.filterBySellerMnemonicAndStatus(sellerMnemonic, status);

    List<Predicate> predicates = mock(List.class);
    when(cb.equal(any(), any())).thenReturn(mock(Predicate.class));
    when(cb.not(any())).thenReturn(mock(Predicate.class));
    when(cb.and(any())).thenReturn(mock(Predicate.class));
    when(root.get(anyString())).thenReturn(mock(Path.class));
    when(root.get(anyString()).get(anyString())).thenReturn(mock(Path.class));
    when(predicates.toArray(any(Predicate[].class))).thenReturn(new Predicate[0]);

    specification.toPredicate(root, query, cb);

    verify(cb).equal(any(), eq(sellerMnemonic));
    verify(cb).equal(any(), eq("O"));
    verify(cb, times(2)).not(any());
  }
}
