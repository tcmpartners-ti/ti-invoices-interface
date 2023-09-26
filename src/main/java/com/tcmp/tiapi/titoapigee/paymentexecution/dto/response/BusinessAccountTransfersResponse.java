package com.tcmp.tiapi.titoapigee.paymentexecution.dto.response;

public record BusinessAccountTransfersResponse(
  ResponseData data
) {
}

record ResponseData(
  String status,
  String paymentId,
  String creationDateTime
) {
}
