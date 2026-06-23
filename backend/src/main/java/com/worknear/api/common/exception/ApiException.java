package com.worknear.api.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base type for all domain/application exceptions. Carries an HTTP status and a
 * stable machine-readable error code.
 */
@Getter
public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String code;

    public ApiException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }
}
