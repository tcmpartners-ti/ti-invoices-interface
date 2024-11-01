package com.tcmp.tiapi.invoice.exception;

import com.tcmp.tiapi.shared.dto.response.error.FieldErrorDetails;
import java.util.List;
import lombok.Getter;

@Getter
public class FieldsInconsistenciesException extends RuntimeException {
  private static final String FIELDS_INCONSISTENCIES_MESSAGE = "Should be the same.";

  private final List<FieldErrorDetails> fieldErrorDetails;

  public FieldsInconsistenciesException(String message, List<String> fields) {
    super(message);
    this.fieldErrorDetails =
        fields.stream().map(f -> new FieldErrorDetails(f, FIELDS_INCONSISTENCIES_MESSAGE)).toList();
  }
}
