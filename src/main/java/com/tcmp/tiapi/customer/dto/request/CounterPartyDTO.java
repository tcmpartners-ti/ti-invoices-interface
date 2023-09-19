package com.tcmp.tiapi.customer.dto.request;

import com.tcmp.tiapi.shared.dto.response.CurrencyAmountDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CounterPartyDTO {
  @Schema(description = "Counter party mnemonic (RUC).", maxLength = 20)
  private String mnemonic;

  @Schema(description = "Counter party name.", maxLength = 35)
  private String name;

  @Schema(description = "Address line 1.", maxLength = 25)
  private String address;

  @Schema(description = "Counter party branch code.", maxLength = 8)
  private String branch;

  @Schema(description = "Counter party status. B = Blocked; R = Referred; A = Active", maxLength = 1)
  private String status;

  @Schema(description = "Invoice limit amount and currency code.")
  CurrencyAmountDTO invoiceLimit;
}
