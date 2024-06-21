package com.tcmp.tiapi.invoice.repository.redis;

import com.tcmp.tiapi.invoice.model.bulkcreate.InvoiceRowProcessingResult;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceRowProcessingResultRepository
    extends CrudRepository<InvoiceRowProcessingResult, String> {
  List<InvoiceRowProcessingResult> findAllByFileUuidOrderByIndex(String fileUuid);

  List<InvoiceRowProcessingResult> findAllByFileUuidAndStatus(
      String fileUuid, InvoiceRowProcessingResult.Status status);

  void deleteAllByFileUuid(String fileUuid);
}
