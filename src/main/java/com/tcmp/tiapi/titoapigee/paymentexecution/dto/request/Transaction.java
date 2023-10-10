package com.tcmp.tiapi.titoapigee.paymentexecution.dto.request;

import lombok.Builder;

@Builder
public record Transaction(
  String concept,
  String amount,
  Currency currency
) {
}
