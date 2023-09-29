package com.tcmp.tiapi.titoapigee.businessbanking.dto.request;

import lombok.Builder;

@Builder
public record PayloadInvoice(
  String batchId,
  String reference,
  String buyerMnemonic
) {
}
