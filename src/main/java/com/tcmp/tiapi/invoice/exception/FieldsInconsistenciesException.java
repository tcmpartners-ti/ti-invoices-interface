package com.tcmp.tiapi.invoice.exception;

import com.tcmp.tiapi.shared.dto.response.error.ErrorDetails;
import lombok.Getter;

import java.util.List;

@Getter
public class FieldsInconsistenciesException extends RuntimeException {
  private static final String FIELDS_INCONSISTENCIES_MESSAGE = "Should be the same.";

  private final List<ErrorDetails> errorDetails;

  public FieldsInconsistenciesException(String message, List<String> fields) {
    super(message);
    this.errorDetails =
        fields.stream().map(f -> new ErrorDetails(f, FIELDS_INCONSISTENCIES_MESSAGE)).toList();
  }
}
