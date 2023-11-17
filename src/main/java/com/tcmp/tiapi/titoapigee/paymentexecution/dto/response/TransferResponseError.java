package com.tcmp.tiapi.titoapigee.paymentexecution.dto.response;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record TransferResponseError(
  String title,
  String detail,
  String instance,
  String type,
  String resource,
  String component,
  String backend
) implements Serializable {
}
