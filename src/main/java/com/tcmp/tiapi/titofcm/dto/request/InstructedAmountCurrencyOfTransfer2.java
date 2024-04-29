package com.tcmp.tiapi.titofcm.dto.request;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InstructedAmountCurrencyOfTransfer2 {
  private String currencyOfTransfer;
  private BigDecimal amount;
}
