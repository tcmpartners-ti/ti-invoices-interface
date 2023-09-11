package com.tcmp.tiapi.shared.exception;


import com.tcmp.tiapi.shared.dto.response.error.ErrorDetails;
import com.tcmp.tiapi.shared.dto.response.error.SimpleHttpErrorMessage;
import com.tcmp.tiapi.shared.dto.response.error.ValidationHttpErrorMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalHttpExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ValidationHttpErrorMessage> handleBodyValidationException(MethodArgumentNotValidException exception) {
    HttpStatus badRequest = HttpStatus.BAD_REQUEST;

    return ResponseEntity.status(badRequest)
      .body(new ValidationHttpErrorMessage(
        badRequest.value(),
        "Could not validate the provided body.",
        exception.getFieldErrors().stream().map(e -> new ErrorDetails(
          e.getField(),
          e.getDefaultMessage()
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
}

