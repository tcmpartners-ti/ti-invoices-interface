package com.tcmp.tiapi.shared.utils;

import jakarta.annotation.Nullable;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MapperUtils {
  public static String trimNullable(@Nullable String value) {
    // Return null because the function will be used inside Mappers.
    if (value == null) return null;
    return value.trim();
  }
}
