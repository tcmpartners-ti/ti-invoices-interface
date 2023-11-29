package com.tcmp.tiapi.titoapigee.businessbanking.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum OperationalGatewayProcessCode {
  INVOICE_CREATED(OperationalGatewayProcessCode.PROCESS_CODE),
  INVOICE_SETTLEMENT(OperationalGatewayProcessCode.PROCESS_CODE),
  INVOICE_FINANCING(OperationalGatewayProcessCode.PROCESS_CODE);

  // Use the same code for everything (Kerlly said so)
  public static final String PROCESS_CODE = "FTI001";

  private final String value;
}
