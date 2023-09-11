package com.tcmp.tiapi.titoapigee.dto.request;

import lombok.Builder;

@Builder
public record OperationalGatewayRequestData(
  ReferenceData referenceData,
  OperationalGatewayRequestPayload payload
) {

}

