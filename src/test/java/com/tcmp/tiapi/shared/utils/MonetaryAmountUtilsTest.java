package com.tcmp.tiapi.shared.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MonetaryAmountUtilsTest {
  @Test
  void convertCentsToDollars_itShouldConvertCentsToDollars() {
    BigDecimal cents = BigDecimal.valueOf(10000); // 100.00 cents
    BigDecimal expectedDollars = BigDecimal.valueOf(100.00).setScale(2, RoundingMode.HALF_UP);

    BigDecimal result = MonetaryAmountUtils.convertCentsToDollars(cents);

    assertEquals(expectedDollars, result);
  }

  @Test
  void convertCentsToDollars_itShouldConvertCentsToDollarsWithRounding() {
    BigDecimal cents = BigDecimal.valueOf(5678);
    BigDecimal expectedDollars = BigDecimal.valueOf(56.78).setScale(2, RoundingMode.HALF_UP);

    BigDecimal result = MonetaryAmountUtils.convertCentsToDollars(cents);

    assertEquals(expectedDollars, result);
  }

  @Test
  void convertCentsToDollars_itShouldConvertCentsToDollarsWithRoundingMock() {
    BigDecimal cents = mock(BigDecimal.class);
    when(cents.setScale(anyInt(), any(RoundingMode.class)))
      .thenReturn(cents); // Mock scale operation
    when(cents.divide(any(BigDecimal.class), any(RoundingMode.class)))
      .thenReturn(BigDecimal.valueOf(0.57)); // Mock division operation

    BigDecimal result = MonetaryAmountUtils.convertCentsToDollars(cents);

    assertEquals(BigDecimal.valueOf(0.57), result);
  }

  @ParameterizedTest
  @CsvSource({
    "10000, 100.00",
    "5678, 56.78",
    "12345, 123.45",
    "99, 0.99",
    "5, 0.05",
    "1, 0.01"
  })
  void convertCentsToDollars_itShouldConvertCentsToDollars(int cents, String expectedDollars) {
    BigDecimal centsBigDecimal = BigDecimal.valueOf(cents);
    BigDecimal expectedDollarsBigDecimal = new BigDecimal(expectedDollars);

    BigDecimal result = MonetaryAmountUtils.convertCentsToDollars(centsBigDecimal);

    assertEquals(expectedDollarsBigDecimal, result);
  }
}
