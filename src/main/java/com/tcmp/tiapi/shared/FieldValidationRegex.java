package com.tcmp.tiapi.shared;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FieldValidationRegex {
  // Date in format dd-MM-yyyy
  public static final String FORMATTED_DATE = "^(0[1-9]|[1-2][0-9]|3[0-1])-(0[1-9]|1[0-2])-\\d{4}$";
  public static final String AVOID_SPECIAL_CHARACTERS = "^[^'\"*;|?&=<>()%\\{\\}\\[\\]\\\\/:\\-]*$";
  public static final String ONLY_NUMERIC_VALUES = "^\\d+$";
  public static final String ONLY_LETTERS = "^[a-zA-Z]+$";
  public static final String NUMBER_WITH_DECIMALS = "^\\d+|\\d+\\.\\d{2}$";
  public static final String BP_BANK_ACCOUNT = "^(AH|CC)\\d{10}$";
  public static final String INVOICE_NUMBER = "^[0-9-]+$";
}
