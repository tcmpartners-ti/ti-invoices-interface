package com.tcmp.tiapi.schedule.model;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum MessageStatus {
  SENT("SENT"),
  PENDING("PENDING");

  private final String value;

  public String value() {
    return value;
  }
}
