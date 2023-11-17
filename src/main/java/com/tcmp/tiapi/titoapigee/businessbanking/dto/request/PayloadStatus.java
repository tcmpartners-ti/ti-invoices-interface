package com.tcmp.tiapi.titoapigee.businessbanking.dto.request;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum PayloadStatus {
  SUCCEEDED("SUCCEEDED"),
  FAILED("FAILED");


  private final String value;
}
