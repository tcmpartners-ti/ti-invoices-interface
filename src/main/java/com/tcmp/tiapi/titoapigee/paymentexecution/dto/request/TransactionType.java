package com.tcmp.tiapi.titoapigee.paymentexecution.dto.request;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum TransactionType {
  BGL_TO_CLIENT("bgl-client-transfer"),
  CLIENT_TO_BGL("client-bgl-transfer");

  private final String value;
}
