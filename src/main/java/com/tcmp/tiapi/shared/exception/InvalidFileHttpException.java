package com.tcmp.tiapi.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.HttpClientErrorException;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidFileHttpException extends HttpClientErrorException {
  public InvalidFileHttpException(String statusText) {
    super(HttpStatus.BAD_REQUEST, statusText);
  }
}
