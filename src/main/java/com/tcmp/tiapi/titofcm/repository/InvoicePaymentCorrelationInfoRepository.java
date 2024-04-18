package com.tcmp.tiapi.titofcm.repository;

import com.tcmp.tiapi.titofcm.model.InvoicePaymentCorrelationInfo;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoicePaymentCorrelationInfoRepository
    extends CrudRepository<InvoicePaymentCorrelationInfo, String> {
  Optional<InvoicePaymentCorrelationInfo> findByPaymentReference(
      String paymentReference);
}
