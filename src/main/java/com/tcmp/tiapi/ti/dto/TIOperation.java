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
  CANCEL_INVOICE_RESULT(TIOperation.CANCEL_INVOICE_RESULT_VALUE),
  FINANCE_INVOICE(TIOperation.FINANCE_INVOICE_VALUE),
  FINANCE_INVOICE_RESULT(TIOperation.FINANCE_INVOICE_RESULT_VALUE),
  SETTLE_INVOICE_RESULT(TIOperation.SETTLE_INVOICE_RESULT_VALUE),
  CREATE_CUSTOMER(TIOperation.CREATE_CUSTOMER_VALUE),
  CREATE_ACCOUNT(TIOperation.CREATE_ACCOUNT_VALUE),
  CREATE_PROGRAMME(TIOperation.CREATE_PROGRAMME_VALUE),
  CREATE_BUYER_OR_SELLER(TIOperation.CREATE_BUYER_OR_SELLER_VALUE),
  CREATE_BUYER_SELLER_RELATIONSHIP(TIOperation.CREATE_BUYER_SELLER_RELATIONSHIP_VALUE),
  CREATE_CUSTOMER_TYPE(TIOperation.CREATE_CUSTOMER_TYPE_VALUE),
  UPDATE_BASE_RATE(TIOperation.UPDATE_BASE_RATE_VALUE),
  FINANCE_SELLER_INVOICE(TIOperation.FINANCE_SELLER_INVOICE_VALUE),
  ITEM("Item");

  public static final String CREATE_INVOICE_VALUE = "TFINVNEW";
  public static final String CREATE_INVOICE_RESULT_VALUE = "TFINVACK";
  public static final String CANCEL_INVOICE_RESULT_VALUE = "TFINVCANA";
  public static final String FINANCE_INVOICE_VALUE = "TFBUYFIN";
  public static final String FINANCE_INVOICE_RESULT_VALUE = "TFBCFCRE";
  public static final String SETTLE_INVOICE_RESULT_VALUE = "TFINVSETCU";
  public static final String CREATE_CUSTOMER_VALUE = "Customer";
  public static final String CREATE_CUSTOMER_TYPE_VALUE = "CustomerType";
  public static final String CREATE_ACCOUNT_VALUE = "Account";
  public static final String CREATE_PROGRAMME_VALUE = "SCFProgramme";
  public static final String CREATE_BUYER_OR_SELLER_VALUE = "SCFBuyerOrSeller";
  public static final String CREATE_BUYER_SELLER_RELATIONSHIP_VALUE = "SCFRelationship";
  public static final String UPDATE_BASE_RATE_VALUE = "InterestSchedule";
  public static final String FINANCE_SELLER_INVOICE_VALUE = "TFSELFIN";

  private final String value;
}
