package com.tcmp.tiapi.titoapigee.paymentexecution.exception;

import com.tcmp.tiapi.titoapigee.paymentexecution.dto.response.TransferResponseError;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentExecutionException extends RuntimeException {
  private final TransferResponseError transferResponseError;

  public PaymentExecutionException(String message) {
    super(message);
    this.transferResponseError = null;
  }
}
