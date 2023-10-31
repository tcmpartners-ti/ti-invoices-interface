package com.tcmp.tiapi.titoapigee.paymentexecution.dto.request;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record TransactionRequest(
  String transactionType,
  Customer debtor,
  Customer creditor,
  Transaction transaction
) {
  public static TransactionRequest from(
    TransactionType type,
    String debtorAccount,
    String creditorAccount,
    String concept,
    BigDecimal amount
  ) {
    return TransactionRequest.builder()
      .transactionType(type.getValue())
      .debtor(Customer.of(debtorAccount))
      .creditor(Customer.of(creditorAccount))
      .transaction(Transaction.builder()
        .concept(concept)
        .amount(amount.toString())
        .currency(new Currency("usd"))
        .build())
      .build();
  }
}
