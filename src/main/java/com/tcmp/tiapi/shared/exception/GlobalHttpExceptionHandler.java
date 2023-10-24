package com.tcmp.tiapi.shared.exception;


import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.tcmp.tiapi.shared.dto.response.error.ErrorDetails;
import com.tcmp.tiapi.shared.dto.response.error.SimpleHttpErrorMessage;
import com.tcmp.tiapi.shared.dto.response.error.ValidationHttpErrorMessage;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalHttpExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ValidationHttpErrorMessage> handleBodyValidationException(MethodArgumentNotValidException exception) {
    HttpStatus badRequest = HttpStatus.BAD_REQUEST;

    return ResponseEntity.status(badRequest)
      .body(new ValidationHttpErrorMessage(
        badRequest.value(),
        "Could not validate the provided fields.",
        exception.getFieldErrors().stream().map(this::buildFieldError).toList()
      ));
  }

  private ErrorDetails buildFieldError(FieldError error) {
    // This exception reveals language information.
    if (error.contains(TypeMismatchException.class)) {
      String message = "Field has an invalid data type for value '%s'".formatted(error.getRejectedValue());
      return new ErrorDetails(error.getField(), message);
    }

    return new ErrorDetails(error.getField(), error.getDefaultMessage());
  }

  @ExceptionHandler(CsvValidationException.class)
  public ResponseEntity<ValidationHttpErrorMessage> handleCsvValidationException(CsvValidationException exception) {
    HttpStatus badRequest = HttpStatus.BAD_REQUEST;

    return ResponseEntity.status(badRequest)
      .body(new ValidationHttpErrorMessage(
        badRequest.value(),
        exception.getMessage(),
        exception.getFieldErrors().stream().map(error -> new ErrorDetails(
          error.getField(),
          error.getDefaultMessage()
        )).toList()
      ));
  }

  @ExceptionHandler(NotFoundHttpException.class)
  public ResponseEntity<SimpleHttpErrorMessage> handleNotFoundException(NotFoundHttpException e) {
    HttpStatus notFound = HttpStatus.NOT_FOUND;

    return ResponseEntity.status(notFound)
      .body(new SimpleHttpErrorMessage(
        notFound.value(),
        e.getMessage()
      ));
  }

  @ExceptionHandler(BadRequestHttpException.class)
  public ResponseEntity<SimpleHttpErrorMessage> handleBadRequestException(BadRequestHttpException e) {
    HttpStatus badRequest = HttpStatus.BAD_REQUEST;

    return ResponseEntity.status(badRequest)
      .body(new SimpleHttpErrorMessage(
        badRequest.value(),
        e.getMessage()
      ));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<SimpleHttpErrorMessage> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
    HttpStatus badRequest = HttpStatus.BAD_REQUEST;

    return ResponseEntity.status(badRequest)
      .body(new SimpleHttpErrorMessage(badRequest.value(), buildFieldTypeErrorMessage(e)));
  }

  private String buildFieldTypeErrorMessage(HttpMessageNotReadableException ex) {
    if (ex.getCause() instanceof InvalidFormatException invalidFormatException) {
      String fieldName = invalidFormatException.getPath().stream()
        .map(JsonMappingException.Reference::getFieldName)
        .collect(Collectors.joining("."));
      String providedValue = invalidFormatException.getValue().toString();

      return String.format("Field '%s' has an invalid data type for the value '%s'.", fieldName, providedValue);
    }

    return "The provided fields have data type inconsistencies.";
  }
}

