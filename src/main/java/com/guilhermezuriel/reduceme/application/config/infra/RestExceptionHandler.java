package com.guilhermezuriel.reduceme.application.config.infra;

import com.guilhermezuriel.reduceme.application.config.exceptions.ApplicationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ApplicationException.class)
    private ResponseEntity<Object> handleApplicationException(ApplicationException exception) {
        return ResponseEntity.status(exception.getStatus()).body(exception.getMessage());
    }
}
