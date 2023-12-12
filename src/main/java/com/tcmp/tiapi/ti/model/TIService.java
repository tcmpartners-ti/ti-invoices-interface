package com.tcmp.tiapi.ti.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TIService {
  TRADE_INNOVATION(TIService.TRADE_INNOVATION_VALUE);

  public static final String TRADE_INNOVATION_VALUE = "TI";

  private final String value;
}
