package com.worknear.api.common.web;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        boolean success,
        String code,
        String message,
        List<FieldError> errors,
        String path,
        Instant timestamp
) {
    public record FieldError(String field, String message) {}

    public static ErrorResponse of(String code, String message, String path) {
        return new ErrorResponse(false, code, message, null, path, Instant.now());
    }

    public static ErrorResponse of(String code, String message, List<FieldError> errors, String path) {
        return new ErrorResponse(false, code, message, errors, path, Instant.now());
    }
}
