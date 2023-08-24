package com.tcmp.tiapi.customer.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum CounterPartyRole {
  BUYER('B'),
  SELLER('S');

  private final Character value;
}
