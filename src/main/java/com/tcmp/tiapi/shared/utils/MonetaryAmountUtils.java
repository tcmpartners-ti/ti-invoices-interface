package com.tcmp.tiapi.shared.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MonetaryAmountUtils {
  private static final int DECIMAL_SCALE = 2;
  private static final BigDecimal ONE_DOLLAR_IN_CENTS = BigDecimal.valueOf(100);

  public static BigDecimal convertCentsToDollars(BigDecimal cents) {
    // Return null because the function will be used inside Mappers.
    if (cents == null) return null;

    RoundingMode roundingMode = RoundingMode.HALF_UP;

    BigDecimal dollars = cents.setScale(DECIMAL_SCALE, roundingMode);
    return dollars.divide(ONE_DOLLAR_IN_CENTS, roundingMode);
  }
}
