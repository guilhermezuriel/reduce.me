package com.guilhermezuriel.reduceme.application.config.exceptions;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Builder
@Getter
@Setter
public class ApplicationException extends RuntimeException {

    private HttpStatus status;
    private String message;

    public ApplicationException(HttpStatus status, String message) {
        super("status: " + status + ", message: " + message);
        this.message = message;
        this.status = status;
    }
}
