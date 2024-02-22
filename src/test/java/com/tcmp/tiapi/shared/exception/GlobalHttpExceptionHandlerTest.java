package com.tcmp.tiapi.shared.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.tcmp.tiapi.invoice.exception.FieldsInconsistenciesException;
import com.tcmp.tiapi.shared.dto.response.error.FieldErrorDetails;
import com.tcmp.tiapi.shared.dto.response.error.SimpleHttpErrorMessage;
import com.tcmp.tiapi.shared.dto.response.error.ValidationHttpErrorMessage;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
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
    when(typeMismatchFieldError.contains(any())).thenReturn(true);

    var actualResponse = httpExceptionHandler.handleBodyValidationException(exception);

    var expectedBody =
        new ValidationHttpErrorMessage(
            HttpStatus.BAD_REQUEST.value(),
            "Could not validate the provided fields.",
            List.of(
                new FieldErrorDetails("field1", "error"),
                new FieldErrorDetails(null, "Field has an invalid data type for value 'null'")));
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
    var exceptionMock = mock(HttpMessageNotReadableException.class);
    var invalidFormatExceptionMock = mock(InvalidFormatException.class);

    when(exceptionMock.getCause()).thenReturn(invalidFormatExceptionMock);
    when(invalidFormatExceptionMock.getValue()).thenReturn("value");
    when(invalidFormatExceptionMock.getPathReference()).thenReturn("path");

    var actualResponse = httpExceptionHandler.handleHttpMessageNotReadableException(exceptionMock);

    var expectedBody =
        new SimpleHttpErrorMessage(
            HttpStatus.BAD_REQUEST.value(),
            "Field '' has an invalid data type for the value 'value'.");
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
    var constraintViolation = mock(ConstraintViolation.class);
    var path = mock(Path.class);
    var node = mock(Path.Node.class);
    var iterator = mock(Iterator.class);

    when(exception.getConstraintViolations()).thenReturn(Set.of(constraintViolation));
    when(constraintViolation.getPropertyPath()).thenReturn(path);
    when(constraintViolation.getPropertyPath().iterator()).thenReturn(iterator);
    when(iterator.next()).thenReturn(node);
    when(node.toString()).thenReturn("field");
    when(iterator.hasNext()).thenReturn(true).thenReturn(false);
    when(path.toString()).thenReturn("path");
    when(constraintViolation.getMessage()).thenReturn("message");

    var actualResponse = httpExceptionHandler.handlePathParamValidationException(exception);

    var expectedBody =
        new ValidationHttpErrorMessage(
            HttpStatus.BAD_REQUEST.value(),
            "Validation failed",
            List.of(new FieldErrorDetails("field", "message")));
    assertEquals(HttpStatus.BAD_REQUEST, actualResponse.getStatusCode());
    assertEquals(expectedBody, actualResponse.getBody());
  }
}
