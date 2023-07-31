package com.tcmp.tiapi.messaging.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum TIService {
    TRADE_INNOVATION(TIService.TRADE_INNOVATION_VALUE);

    public static final String TRADE_INNOVATION_VALUE = "TI";

    @Getter
    private final String value;
}
