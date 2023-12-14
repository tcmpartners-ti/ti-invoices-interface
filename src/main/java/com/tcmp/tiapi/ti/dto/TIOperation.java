package com.tcmp.tiapi.ti.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/***
 * An enum with public constants is used to restrict the values that a function param can have and also
 * to consume the operation values from annotations.
 */
@RequiredArgsConstructor
@Getter
public enum TIOperation {
  CREATE_INVOICE(TIOperation.CREATE_INVOICE_VALUE),
  CREATE_INVOICE_RESULT(TIOperation.CREATE_INVOICE_RESULT_VALUE),
  FINANCE_INVOICE(TIOperation.FINANCE_INVOICE_VALUE),
  FINANCE_INVOICE_RESULT(TIOperation.FINANCE_INVOICE_RESULT_VALUE),
  SETTLE_INVOICE(TIOperation.SETTLE_INVOICE_VALUE);

  public static final String CREATE_INVOICE_VALUE = "TFINVNEW";
  public static final String FINANCE_INVOICE_VALUE = "TFBUYFIN";
  public static final String FINANCE_INVOICE_RESULT_VALUE = "TFBCFCRE";
  public static final String SETTLE_INVOICE_VALUE = "TFINVSETCU";
  public static final String CREATE_INVOICE_RESULT_VALUE = "TFINVACK";

  private final String value;
}
