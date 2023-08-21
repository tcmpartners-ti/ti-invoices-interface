package com.tcmp.tiapi.shared.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.HttpClientErrorException;

@ResponseStatus(HttpStatus.BAD_REQUEST)
@Getter
public class InvalidFileHttpException extends HttpClientErrorException {
  private final String message;

  public InvalidFileHttpException(String message) {
    super(HttpStatus.BAD_REQUEST, message);
    this.message = message;
  }
}
