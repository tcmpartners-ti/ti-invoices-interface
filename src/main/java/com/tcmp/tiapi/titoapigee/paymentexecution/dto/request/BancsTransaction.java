package com.tcmp.tiapi.titoapigee.paymentexecution.dto.request;

import lombok.Builder;

@Builder
public record BancsTransaction(
  String concept,
  String amount,
  TransactionCurrency currency
) {
}
