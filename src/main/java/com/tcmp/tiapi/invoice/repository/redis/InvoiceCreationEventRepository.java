package com.tcmp.tiapi.invoice.repository.redis;

import com.tcmp.tiapi.invoice.model.InvoiceEventInfo;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceCreationEventRepository extends CrudRepository<InvoiceEventInfo, String> {}
