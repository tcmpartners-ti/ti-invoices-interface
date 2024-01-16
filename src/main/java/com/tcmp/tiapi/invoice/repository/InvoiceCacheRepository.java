package com.tcmp.tiapi.invoice.repository;

import com.tcmp.tiapi.invoice.model.InvoiceEventInfo;
import java.util.List;

public interface InvoiceCacheRepository {
  void saveAll(List<InvoiceEventInfo> invoiceEvents);
}
