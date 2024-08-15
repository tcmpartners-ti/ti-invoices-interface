package com.tcmp.tiapi.shared.utils;

import jakarta.annotation.Nullable;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StringMappingUtils {
  public static String trimNullable(@Nullable String nullableString) {
    // Return null because the function will be used inside Mappers.
    if (nullableString == null) return null;
    return nullableString.trim();
  }

  public static String toStringNullable(Object value) {
    if (value == null) return "";
    return value.toString();
  }
}
