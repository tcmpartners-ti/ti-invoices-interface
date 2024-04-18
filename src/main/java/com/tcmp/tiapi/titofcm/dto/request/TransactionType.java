package com.tcmp.tiapi.titofcm.dto.request;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TransactionType {
  public static final String CREDIT = "CREDIT";
  public static final String DEBIT = "DEBIT";
}
