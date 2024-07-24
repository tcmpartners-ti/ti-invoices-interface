package com.tcmp.tiapi.invoice.repository.redis;

public interface InvoiceRowProcessingResultBatchRepository {
  long totalRowsByIdPattern(String idPattern);
}
