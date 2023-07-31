package com.tcmp.tiapi.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundHttpException extends RuntimeException {
    public NotFoundHttpException(String message) {
        super(message);
    }
}
