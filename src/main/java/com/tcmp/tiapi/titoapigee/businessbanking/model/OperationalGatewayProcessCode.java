package com.tcmp.tiapi.titoapigee.businessbanking.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum OperationalGatewayProcessCode {
  INVOICE_CREATED(OperationalGatewayProcessCode.PROCESS_CODE),
  INVOICE_SETTLEMENT(OperationalGatewayProcessCode.PROCESS_CODE),
  INVOICE_FINANCING(OperationalGatewayProcessCode.PROCESS_CODE),
  INVOICE_SETTLEMENT_SFTP(OperationalGatewayProcessCode.PROCESS_CODE_SFTP),
  INVOICE_FINANCING_SFTP(OperationalGatewayProcessCode.PROCESS_CODE_SFTP);

  // Use the same code for everything (Kerly said so)
  public static final String PROCESS_CODE = "FTI001";
  public static final String PROCESS_CODE_SFTP = "FTI002";

  private final String value;
}
