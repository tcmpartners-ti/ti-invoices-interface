package com.tcmp.tiapi.invoice.repository.redis;

public interface InvoiceProcessingRowBulkRepository {
  long totalRowsByIdPattern(String idPattern);
}
