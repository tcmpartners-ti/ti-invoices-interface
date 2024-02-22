package com.tcmp.tiapi.shared.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.tcmp.tiapi.invoice.exception.FieldsInconsistenciesException;
import com.tcmp.tiapi.shared.dto.response.error.FieldErrorDetails;
import com.tcmp.tiapi.shared.dto.response.error.SimpleHttpErrorMessage;
import com.tcmp.tiapi.shared.dto.response.error.ValidationHttpErrorMessage;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

class GlobalHttpExceptionHandlerTest {

  private GlobalHttpExceptionHandler httpExceptionHandler;

  @BeforeEach
  void setUp() {
    httpExceptionHandler = new GlobalHttpExceptionHandler();
  }

  @Test
  void handleBodyValidationException() {
    var exception = mock(MethodArgumentNotValidException.class);
    var typeMismatchFieldError = mock(FieldError.class);
    when(exception.getFieldErrors())
        .thenReturn(List.of(new FieldError("object", "field1", "error"), typeMismatchFieldError));

    var actualResponse = httpExceptionHandler.handleBodyValidationException(exception);

    var expectedBody =
        new ValidationHttpErrorMessage(
            HttpStatus.BAD_REQUEST.value(),
            "Could not validate the provided fields.",
            List.of(new FieldErrorDetails("field1", "error")));
    assertEquals(HttpStatus.BAD_REQUEST, actualResponse.getStatusCode());
    assertEquals(expectedBody, actualResponse.getBody());
  }

  @Test
  void handleNotFoundException() {
    var exception = new NotFoundHttpException("Not Found!");

    var actualResponse = httpExceptionHandler.handleNotFoundException(exception);

    var expectedBody =
        new SimpleHttpErrorMessage(HttpStatus.NOT_FOUND.value(), exception.getMessage());
    assertEquals(HttpStatus.NOT_FOUND, actualResponse.getStatusCode());
    assertEquals(expectedBody, actualResponse.getBody());
  }

  @Test
  void handleBadRequestException() {
    var exception = new BadRequestHttpException("Bad Request!");

    var actualResponse = httpExceptionHandler.handleBadRequestException(exception);

    var expectedBody =
        new SimpleHttpErrorMessage(HttpStatus.BAD_REQUEST.value(), exception.getMessage());
    assertEquals(HttpStatus.BAD_REQUEST, actualResponse.getStatusCode());
    assertEquals(expectedBody, actualResponse.getBody());
  }

  @Test
  void handleHttpMessageNotReadableException() {

    var exception = mock(HttpMessageNotReadableException.class);

    var actualResponse = httpExceptionHandler.handleHttpMessageNotReadableException(exception);

    var expectedBody =
        new SimpleHttpErrorMessage(
            HttpStatus.BAD_REQUEST.value(), "The provided fields have data type inconsistencies.");
    assertEquals(HttpStatus.BAD_REQUEST, actualResponse.getStatusCode());
    assertEquals(expectedBody, actualResponse.getBody());
  }

  @Test
  void handleFieldsInconsistenciesException() {
    var exception = new FieldsInconsistenciesException("Field errors", List.of());

    var actualResponse = httpExceptionHandler.handleFieldsInconsistenciesException(exception);

    var expectedBody =
        new ValidationHttpErrorMessage(
            HttpStatus.BAD_REQUEST.value(), "Fields inconsistencies error.", List.of());
    assertEquals(HttpStatus.BAD_REQUEST, actualResponse.getStatusCode());
    assertEquals(expectedBody, actualResponse.getBody());
  }

  @Test
  void handlePathParamValidationException() {
    var exception = mock(ConstraintViolationException.class);

    var actualResponse = httpExceptionHandler.handlePathParamValidationException(exception);

    var expectedBody =
        new ValidationHttpErrorMessage(
            HttpStatus.BAD_REQUEST.value(), "Validation failed", List.of());
    assertEquals(HttpStatus.BAD_REQUEST, actualResponse.getStatusCode());
    assertEquals(expectedBody, actualResponse.getBody());
  }
}
