package com.tcmp.tiapi.titoapigee.paymentexecution.dto.request;

import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record TransactionRequest(
  String transactionType,
  Customer debtor,
  Customer creditor,
  Transaction transaction
) {
  private static final int MAX_CONCEPT_LENGTH = 50;

  public static TransactionRequest from(
    TransactionType type,
    String debtorAccount,
    String creditorAccount,
    String concept,
    String currency,
    BigDecimal amount
  ) {
    String sanitizedConcept = concept.replaceAll("[^a-zA-Z0-9\\s-]", "");

    if (sanitizedConcept.length() > MAX_CONCEPT_LENGTH) {
      sanitizedConcept = sanitizedConcept.substring(0, MAX_CONCEPT_LENGTH);
    }

    return TransactionRequest.builder()
      .transactionType(type.getValue())
      .debtor(Customer.of(debtorAccount))
      .creditor(Customer.of(creditorAccount))
      .transaction(Transaction.builder()
        .concept(sanitizedConcept)
        .amount(amount.toString())
        .currency(new Currency(currency))
        .build())
      .build();
  }
}
