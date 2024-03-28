package com.tcmp.tiapi.ti.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TIService {
  TRADE_INNOVATION(TIService.TRADE_INNOVATION_VALUE),
  TRADE_INNOVATION_BULK(TIService.TRADE_INNOVATION_BULK_VALUE);

  public static final String TRADE_INNOVATION_VALUE = "TI";
  public static final String TRADE_INNOVATION_BULK_VALUE = "TIBulk";

  private final String value;
}
