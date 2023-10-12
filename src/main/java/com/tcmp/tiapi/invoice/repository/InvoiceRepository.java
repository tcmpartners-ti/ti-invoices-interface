package com.tcmp.tiapi.invoice.repository;

import com.tcmp.tiapi.invoice.model.InvoiceMaster;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<InvoiceMaster, Long> {
  Optional<InvoiceMaster> findByProgramIdAndSellerMnemonicAndReferenceAndProductMasterIsActive(
    String programId,
    String sellerMnemonic,
    String invoiceReference,
    boolean isActive
  );

  Page<InvoiceMaster> findAll(Specification<InvoiceMaster> spec, Pageable pageable);
}
