package com.tcmp.tiapi.titofcm.dto.request;

import java.math.BigDecimal;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class InstructedAmountCurrencyOfTransfer2 {
  private String currencyOfTransfer;
  private BigDecimal amount;
}
