package com.tcmp.tiapi.titoapigee.corporateloan.dto.request;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record Tax(
  String code,
  PaymentForm paymentForm,
  BigDecimal rate,
  BigDecimal amount
) {
}
