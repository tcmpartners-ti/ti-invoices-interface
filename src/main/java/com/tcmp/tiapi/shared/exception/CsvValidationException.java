package com.tcmp.tiapi.shared.exception;

import java.util.List;
import lombok.Getter;
import org.springframework.validation.FieldError;

@Getter
public class CsvValidationException extends RuntimeException {
  private final List<FieldError> fieldErrors;

  public CsvValidationException(String message, List<FieldError> fieldErrors) {
    super(message);
    this.fieldErrors = fieldErrors;
  }
}
