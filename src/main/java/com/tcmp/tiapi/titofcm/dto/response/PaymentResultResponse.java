package com.tcmp.tiapi.titofcm.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

@Builder
public record PaymentResultResponse(Status status, String paymentReference, Type type) {
  public enum Status {
    SUCCEEDED,
    FAILED
  }

  @RequiredArgsConstructor
  public enum Type {
    @JsonProperty("client-bgl-transfer")
    CLIENT_BGL("client-bgl-transfer"), // Debit
    @JsonProperty("bgl-client-transfer")
    BGL_CLIENT("bgl-client-transfer"); // Credit

    private final String value;

    @JsonValue
    public String value() {
      return value;
    }
  }
}
