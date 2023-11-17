package com.tcmp.tiapi.titoapigee.businessbanking.dto.request;

import lombok.Builder;

@Builder
public record OperationalGatewayRequest<T>(
  ReferenceData referenceData,
  T payload
) {

}

