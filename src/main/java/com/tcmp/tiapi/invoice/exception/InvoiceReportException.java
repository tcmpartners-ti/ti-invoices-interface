package com.tcmp.tiapi.invoice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class InvoiceReportException extends RuntimeException {
  public InvoiceReportException(String message) {
    super(message);
  }
}
