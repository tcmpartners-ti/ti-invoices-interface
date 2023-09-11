package com.tcmp.tiapi.titoapigee.dto.request;

import lombok.Builder;

@Builder
public record OperationalGatewayRequestPayload(
  String status,
  PayloadInvoice invoice,
  PayloadDetails details
) {
}
