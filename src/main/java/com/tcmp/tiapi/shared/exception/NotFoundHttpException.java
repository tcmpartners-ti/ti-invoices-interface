package com.tcmp.tiapi.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.HttpClientErrorException;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundHttpException extends HttpClientErrorException {
  public NotFoundHttpException(String statusText) {
    super(HttpStatus.NOT_FOUND, statusText);
  }
}
