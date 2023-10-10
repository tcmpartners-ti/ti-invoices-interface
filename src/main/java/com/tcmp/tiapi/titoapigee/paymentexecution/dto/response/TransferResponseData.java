package com.tcmp.tiapi.titoapigee.paymentexecution.dto.response;

public record TransferResponseData(
  String status,
  String paymentId,
  String creationDateTime
) {
}
