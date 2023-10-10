package com.tcmp.tiapi.titoapigee.paymentexecution.dto.request;

import lombok.Builder;

@Builder
public record Currency(
  String code
) {
}
