package com.tcmp.tiapi.invoice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record InvoiceRealOutputData(
    String invoiceReference,
    LocalDateTime processedAt,
    Status status,
    BigDecimal amount,
    String counterPartyMnemonic) {
  public enum Status {
    FINANCED,
    PAID,
    FAILED;

    public String tsvValue() {
      return switch (this) {
        case FINANCED -> "Financiado";
        case PAID -> "Pagado";
        case FAILED -> "Fallido";
      };
    }
  }
}
