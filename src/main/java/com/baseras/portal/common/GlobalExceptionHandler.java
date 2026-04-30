package com.baseras.portal.common;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    public record ApiError(
            String type, String title, int status, String detail,
            String path, OffsetDateTime timestamp, Map<String, String> errors
    ) {}

    @ExceptionHandler(AppExceptions.ApiException.class)
    public ResponseEntity<ApiError> handleApi(AppExceptions.ApiException ex, HttpServletRequest req) {
        return ResponseEntity.status(ex.getStatus()).body(
                new ApiError("about:blank", ex.getTitle(), ex.getStatus().value(),
                        ex.getMessage(), req.getRequestURI(), OffsetDateTime.now(), null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e ->
                errors.put(e.getField(), e.getDefaultMessage()));
        return ResponseEntity.badRequest().body(
                new ApiError("about:blank", "Validation Failed", 400,
                        "One or more fields are invalid", req.getRequestURI(), OffsetDateTime.now(), errors));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                new ApiError("about:blank", "Forbidden", 403, "Not allowed",
                        req.getRequestURI(), OffsetDateTime.now(), null));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuth(AuthenticationException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new ApiError("about:blank", "Unauthorized", 401, "Authentication required",
                        req.getRequestURI(), OffsetDateTime.now(), null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAny(Exception ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ApiError("about:blank", "Internal Server Error", 500,
                        ex.getMessage() != null ? ex.getMessage() : "Unexpected error",
                        req.getRequestURI(), OffsetDateTime.now(), null));
    }
}
