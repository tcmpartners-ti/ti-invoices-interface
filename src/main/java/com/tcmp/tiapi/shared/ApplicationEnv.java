package com.tcmp.tiapi.shared;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ApplicationEnv {
  LOCAL("local"),
  DEV("dev");

  private final String value;

  public String value() {
    return this.value;
  }
}
