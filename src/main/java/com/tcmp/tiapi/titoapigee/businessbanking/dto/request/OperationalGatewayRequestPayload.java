package com.tcmp.tiapi.titoapigee.businessbanking.dto.request;

import lombok.Builder;

@Builder
public record OperationalGatewayRequestPayload(
  String status,
  PayloadInvoice invoice,
  PayloadDetails details
) {
}
