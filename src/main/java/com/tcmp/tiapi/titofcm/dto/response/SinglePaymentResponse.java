package com.tcmp.tiapi.titofcm.dto.response;

public record SinglePaymentResponse(Data data) {
  public record Data(String paymentReferenceNumber) {}
}
