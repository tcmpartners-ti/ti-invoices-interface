package com.tcmp.tiapi.invoice.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record InvoiceSearchParams(
  @NotNull
  Long program,
  @NotNull
  Long seller
) {
}
