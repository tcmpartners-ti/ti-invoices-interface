package com.tcmp.tiapi.invoice.dto.request;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record InvoiceNotificationPayload(
  String id,
  String batchId,
  String invoiceNumber,
  String buyer
) implements Serializable {
}
