package com.tcmp.tiapi.invoice.validation;

import com.tcmp.tiapi.invoice.dto.InvoiceCreationRowCSV;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.Validator;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;

@Slf4j
public class InvoiceRowValidator {
  public List<FieldError> validate(int line, InvoiceCreationRowCSV invoiceRow, Validator validator) {
    Set<ConstraintViolation<InvoiceCreationRowCSV>> violations = validator.validate(invoiceRow);

    return violations.stream().map(violation -> new FieldError(
        violation.getLeafBean().toString(),
        violation.getPropertyPath().toString(),
        buildCustomErrorMessage(line, violation.getPropertyPath(), violation.getMessage())
      )
    ).toList();
  }

  private String buildCustomErrorMessage(int line, Path path, String baseErrorMessage) {
    return String.format("[Line %d] Field '%s' error: %s", line, path, baseErrorMessage);
  }
}
