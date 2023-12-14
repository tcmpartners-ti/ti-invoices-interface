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
  FINANCE_INVOICE(TIOperation.FINANCE_INVOICE_VALUE),
  FINANCE_ACK_INVOICE(TIOperation.FINANCE_ACK_INVOICE_VALUE),
  FINANCE_ACK_INVOICE_DETAILS(TIOperation.FINANCE_ACK_INVOICE_DETAILS_VALUE),
  DUE_INVOICE(TIOperation.DUE_INVOICE_VALUE),
  DUE_INVOICE_DETAILS(TIOperation.DUE_INVOICE_DETAILS_VALUE),
  NOTIFICATION_CREATION_ACK_INVOICE(TIOperation.NOTIFICATION_CREATION_ACK_INVOICE_VALUE),
  NOTIFICATION_CREATION_ACK_INVOICE_DETAILS(
      TIOperation.NOTIFICATION_CREATION_ACK_INVOICE_DETAILS_VALUE);

  public static final String CREATE_INVOICE_VALUE = "TFINVNEW";
  public static final String FINANCE_INVOICE_VALUE = "TFBUYFIN";
  public static final String FINANCE_ACK_INVOICE_VALUE = "TFBCFCRE";
  public static final String FINANCE_ACK_INVOICE_DETAILS_VALUE = "tfinvfindet"; // Body tag name
  public static final String DUE_INVOICE_VALUE = "TFINVSETCU";
  public static final String DUE_INVOICE_DETAILS_VALUE = "tfinvset"; // Body tag name
  public static final String NOTIFICATION_CREATION_ACK_INVOICE_VALUE = "TFINVACK";
  public static final String NOTIFICATION_CREATION_ACK_INVOICE_DETAILS_VALUE = "tfinvdet";

  private final String value;
}
