package com.tcmp.tiapi.titoapigee.businessbanking.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum OperationalGatewayProcessCode {
  INVOICE_CREATION("FTI001"),
  INVOICE_SETTLEMENT("FTI003"),
  ADVANCE_INVOICE_DISCOUNT("FTI002"),
  INVOICE_DUE_DATE_REACHED("CRPR03");

  private final String value;
}
