package com.tcmp.tiapi.titoapigee.corporateloan.dto.response;

public record Tax(
  String code,
  PaymentForm paymentForm,
  double factor,
  double amount,
  String text,
  String note
) {
}
