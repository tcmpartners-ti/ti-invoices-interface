package com.tcmp.tiapi.ti.dto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TIOperationTag {
  public static final String INVOICE_SETTLEMENT = "tfinvset";
  public static final String INVOICE_FINANCING_RESULT = "tfinvfindet";
  public static final String INVOICE_CREATION_RESULT = "tfinvdet";
  public static final String INVOICE_CANCELLATION_RESULT = "tfinvgen";
}
