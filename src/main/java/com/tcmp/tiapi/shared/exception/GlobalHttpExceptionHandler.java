package com.tcmp.tiapi.shared.exception;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;
import java.util.Optional;

@ControllerAdvice
public class GlobalHttpExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleBodyValidationException(MethodArgumentNotValidException exception) {
    HttpStatus badRequest = HttpStatus.BAD_REQUEST;

    return ResponseEntity.status(badRequest)
      .body(Map.of(
        "statusCode", badRequest.value(),
        "message", "Could not validate the provided body.",
        "errors", exception.getFieldErrors().stream().map(error -> Map.of(
          "field", error.getField(),
          "error", Optional.ofNullable(error.getDefaultMessage()).orElse("Internal error.")
        )).toList()
      ));
  }

  @ExceptionHandler(NotFoundHttpException.class)
  public ResponseEntity<Map<String, Object>> handleNotFoundException(NotFoundHttpException e) {
    HttpStatus notFound = HttpStatus.NOT_FOUND;

    return ResponseEntity.status(notFound)
      .body(Map.of(
        "statusCode", notFound.value(),
        "error", e.getMessage()
      ));
  }
}
