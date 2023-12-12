package com.tcmp.tiapi.ti.model.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ResponseStatus {
  SUCCESS("SUCCEEDED"),
  FAILED("FAILED"),
  UNAVAILABLE("UNAVAILABLE");

  private final String value;
}
