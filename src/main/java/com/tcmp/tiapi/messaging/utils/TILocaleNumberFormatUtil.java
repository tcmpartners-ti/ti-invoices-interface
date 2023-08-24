package com.tcmp.tiapi.messaging.utils;

import org.mapstruct.Qualifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class TILocaleNumberFormatUtil {
  @Qualifier
  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.SOURCE)
  public @interface TILocalizedNumber {
  }

  DecimalFormat decimalFormat = new DecimalFormat(
    "#,##0.00", new DecimalFormatSymbols(Locale.US));

  @TILocalizedNumber
  public String tiLocalizedNumber(BigDecimal input) {
    return decimalFormat.format(input);
  }
}
