package com.tcmp.tiapi.shared.exception;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

@ControllerAdvice
public class GlobalHttpExceptionHandler {
  @ExceptionHandler(NotFoundHttpException.class)
  public ResponseEntity<Map<String, Object>> handleNotFoundException(NotFoundHttpException e) {
    return ResponseEntity.status(e.getStatusCode().value())
      .body(Map.of("error", e.getMessage()));
  }
}
