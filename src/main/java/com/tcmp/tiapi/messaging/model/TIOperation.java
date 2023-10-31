package com.tcmp.tiapi.messaging.model;

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
  FINANCE_ACK_INVOICE(TIOperation.FINANCE_ACK_INVOICE_VALUE);

  public static final String CREATE_INVOICE_VALUE = "TFINVNEW";
  public static final String FINANCE_INVOICE_VALUE = "TFBUYFIN";
  public static final String FINANCE_ACK_INVOICE_VALUE = "TFBCFCRE";

  private final String value;
}
