package com.tcmp.tiapi.titoapigee.paymentexecution.dto.request;

import lombok.Builder;

@Builder
public record TransactionRequest(
  String transactionType,
  BancsCustomer debtor,
  BancsCustomer creditor,
  BancsTransaction transaction
) {
}
