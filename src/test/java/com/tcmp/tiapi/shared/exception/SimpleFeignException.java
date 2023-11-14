package com.tcmp.tiapi.shared.exception;

import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class SimpleFeignException extends FeignException {
  public SimpleFeignException(int status, String message) {
    super(status, message);
  }
}
