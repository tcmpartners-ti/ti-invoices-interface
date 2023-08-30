package com.tcmp.tiapi.invoice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InvoiceCounterPartyDTO {
  @Schema(name = "id", description = "Counter party id")
  private final Long id;

  @Schema(name = "mnemonic", description = "Counter party mnemonic (RUC).")
  private final String mnemonic;

  @Schema(name = "name", description = "Counter party name.")
  private final String name;
}
