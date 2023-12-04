package com.tcmp.tiapi.titoapigee.paymentexecution.dto.request;

import lombok.Builder;

@Builder
public record Customer(Account account) {
  public static Customer of(String account) {
    return new Customer(new Account(account));
  }
}
