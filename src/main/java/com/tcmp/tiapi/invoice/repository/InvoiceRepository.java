package com.tcmp.tiapi.invoice.repository;

import com.tcmp.tiapi.invoice.model.InvoiceMaster;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<InvoiceMaster, Long> {
  Optional<InvoiceMaster> findFirstByProgrammeIdAndSellerIdAndReference(Long programmeId, Long sellerId, String reference);

  Optional<InvoiceMaster> findFirstByReference(String reference);

  Page<InvoiceMaster> findByBuyerIdIn(List<Long> buyerIds, Pageable pageable);

  Page<InvoiceMaster> findAll(Specification<InvoiceMaster> spec, Pageable pageable);
}
