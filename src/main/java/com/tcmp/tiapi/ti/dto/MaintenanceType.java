package com.tcmp.tiapi.ti.dto;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum MaintenanceType {
  DEFINE("F"),
  DELETE("D");

  private final String value;

  public String value() {
    return value;
  }
}
