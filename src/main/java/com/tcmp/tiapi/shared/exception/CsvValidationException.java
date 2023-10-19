package com.tcmp.tiapi.shared.exception;

import lombok.Getter;
import org.springframework.validation.FieldError;

import java.util.List;

@Getter
public class CsvValidationException extends RuntimeException {
  private final List<FieldError> fieldErrors;

  public CsvValidationException(String message, List<FieldError> fieldErrors) {
    super(message);
    this.fieldErrors = fieldErrors;
  }
}
