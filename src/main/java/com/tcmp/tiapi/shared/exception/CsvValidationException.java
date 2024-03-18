package com.tcmp.tiapi.shared.exception;

import java.util.List;
import lombok.Getter;

@Getter
public class CsvValidationException extends RuntimeException {
  private final List<String> fieldErrors;

  public CsvValidationException(String message, List<String> fieldErrors) {
    super(message);
    this.fieldErrors = fieldErrors;
  }
}
