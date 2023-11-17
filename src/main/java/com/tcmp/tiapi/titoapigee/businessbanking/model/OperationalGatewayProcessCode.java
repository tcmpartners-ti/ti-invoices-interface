package com.tcmp.tiapi.titoapigee.businessbanking.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum OperationalGatewayProcessCode {
  INVOICE_CREATED("FTI001"),
  INVOICE_SETTLEMENT("FTI001"),
  INVOICE_FINANCING("FTI002");

  private final String value;
}
