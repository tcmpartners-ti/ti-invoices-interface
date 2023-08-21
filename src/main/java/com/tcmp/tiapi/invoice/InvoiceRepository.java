package com.tcmp.tiapi.invoice;

import com.tcmp.tiapi.invoice.model.InvoiceMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<InvoiceMaster, Long> {
  Optional<InvoiceMaster> findByReference(String reference);
}
