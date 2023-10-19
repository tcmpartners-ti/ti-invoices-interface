package com.tcmp.tiapi.shared;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FieldValidationRegex {
  // Date in format dd-MM-yyyy
  public static final String FORMATTED_DATE = "^(0[1-9]|[1-2][0-9]|3[0-1])-(0[1-9]|1[0-2])-\\d{4}$";
  public static final String AVOID_SPECIAL_CHARACTERS = "^[^'\"*;|?&=<>()%\\{\\}\\[\\]\\\\/:-]+$";
  public static final String ONLY_NUMERIC_VALUES = "^\\d+$";
}
