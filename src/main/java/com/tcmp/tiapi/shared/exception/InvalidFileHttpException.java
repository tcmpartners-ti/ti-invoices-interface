package com.tcmp.tiapi.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidFileHttpException extends RuntimeException {
    public InvalidFileHttpException(String message) {
        super(message);
    }
}
