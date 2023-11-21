package com.tcmp.tiapi.shared;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ApplicationEnv {
  LOCAL("local"),
  DEV("dev"),
  QA("qa"),
  PROD("prod");

  private final String value;

  public String value() {
    return this.value;
  }
}
