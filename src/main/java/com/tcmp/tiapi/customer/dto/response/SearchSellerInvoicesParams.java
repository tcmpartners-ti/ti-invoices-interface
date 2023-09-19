package com.tcmp.tiapi.customer.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record SearchSellerInvoicesParams(
  @Size(min = 1, max = 1, message = "Invoice status must be up to 1 character long.")
  @Pattern(regexp = "[OLPDEC]", message = "Invoice status must be either O = Outstanding; L = Overdue; P = Paid; D = Inquiry; E = Dishonoured or C = Cancelled.")
  @Nullable
  @Schema(description = "Invoice status to filter by. If not set, invoices with every status will be returned.")
  String invoiceStatus
) {
}
