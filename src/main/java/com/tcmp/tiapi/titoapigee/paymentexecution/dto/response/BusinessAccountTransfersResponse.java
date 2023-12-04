package com.tcmp.tiapi.titoapigee.paymentexecution.dto.response;

public record BusinessAccountTransfersResponse(TransferResponseData data) {
  public boolean isOk() {
    if (data == null) return false;
    return "OK".equals(data.status());
  }
}
