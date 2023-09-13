package com.tcmp.tiapi.titoapigee.dto.request;

import lombok.Builder;

@Builder
public record ApiGeeBaseRequest<T>(
  T data
) {
}

