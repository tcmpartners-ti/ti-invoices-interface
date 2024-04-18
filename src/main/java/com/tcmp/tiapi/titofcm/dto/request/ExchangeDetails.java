package com.tcmp.tiapi.titofcm.dto.request;

import lombok.Builder;

@Builder
public record ExchangeDetails(
    String exchangeRateType, String contractRefNumber, long exchangeRateInformation) {}
