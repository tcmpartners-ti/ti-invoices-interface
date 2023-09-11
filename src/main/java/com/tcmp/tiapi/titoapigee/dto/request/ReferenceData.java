package com.tcmp.tiapi.titoapigee.dto.request;

import lombok.Builder;

@Builder
public record ReferenceData(
  String provider,
  String correlatedMessageId,
  ProcessCode processCode
) {
}
