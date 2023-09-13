package com.tcmp.tiapi.titoapigee.dto.request;

import lombok.Builder;

@Builder
public record OperationalGatewayRequest(
  ReferenceData referenceData,
  OperationalGatewayRequestPayload payload
) {

}

