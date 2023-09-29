package com.tcmp.tiapi.titoapigee.businessbanking.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum OperationalGatewayProcessCode {
  INVOICE_CREATION("CRPR01"),
  ADVANCE_INVOICE_DISCOUNT("CRPR02");

  private final String value;
}
