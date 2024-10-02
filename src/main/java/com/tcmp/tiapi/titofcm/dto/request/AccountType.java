package com.tcmp.tiapi.titofcm.dto.request;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AccountType {
  public static final String SAVINGS = "Savings Account";
  public static final String GENERALLEDGER = "GL Account";
}
