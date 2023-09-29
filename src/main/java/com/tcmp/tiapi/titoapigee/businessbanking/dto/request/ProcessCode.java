package com.tcmp.tiapi.titoapigee.businessbanking.dto.request;

import com.tcmp.tiapi.titoapigee.businessbanking.model.OperationalGatewayProcessCode;

public record ProcessCode(
  String code
) {
  public static ProcessCode of(OperationalGatewayProcessCode processCode) {
    return new ProcessCode(processCode.getValue());
  }
}
