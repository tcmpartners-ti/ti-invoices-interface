package com.tcmp.tiapi.titoapigee.dto.request;

import lombok.Builder;

@Builder
public record PayloadInvoice(
  String batchId, // Might be complicated
  String invoiceNumber,
  String buyer // Might be complicated
) {
}
