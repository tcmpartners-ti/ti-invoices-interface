package com.tcmp.tiapi.titoapigee.corporateloan.dto.request;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tcmp.tiapi.shared.serializer.JsonMoneySerializer;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record Tax(
  String code,
  PaymentForm paymentForm,
  @JsonSerialize(using = JsonMoneySerializer.class)
  BigDecimal rate,
  @JsonSerialize(using = JsonMoneySerializer.class)
  BigDecimal amount
) {
}
