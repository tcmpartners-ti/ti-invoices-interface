package com.tcmp.tiapi.invoice.dto.request;

public record InvoiceCorrelationPayload(
  String batchId,
  String invoiceNumber,
  String buyer
) {
}
