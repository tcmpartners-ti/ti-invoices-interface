package com.tcmp.tiapi.program.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum PartyType {
  BUYER("B"),
  SELLER("S");

  private final String value;
}
