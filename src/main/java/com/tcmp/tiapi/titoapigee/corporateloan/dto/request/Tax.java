package com.tcmp.tiapi.titoapigee.corporateloan.dto.request;

import java.math.BigDecimal;

public record Tax(
  String code,
  PaymentForm paymentForm,
  BigDecimal rate,
  BigDecimal amount
) {
}
