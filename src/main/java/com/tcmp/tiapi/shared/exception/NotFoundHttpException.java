package com.tcmp.tiapi.shared.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.HttpClientErrorException;

@ResponseStatus(HttpStatus.NOT_FOUND)
@Getter
public class NotFoundHttpException extends HttpClientErrorException {
  private final String message;

  public NotFoundHttpException(String message) {
    super(HttpStatus.NOT_FOUND, message);
    this.message = message;
  }
}
