package com.tcmp.tiapi.titofcm.exception;

public class SinglePaymentException extends RuntimeException {
  public SinglePaymentException(String message) {
    super(message);
  }

  public SinglePaymentException(String message, Throwable cause) {
    super(message, cause);
  }
}
