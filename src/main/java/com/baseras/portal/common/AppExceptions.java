package com.baseras.portal.common;

import org.springframework.http.HttpStatus;

public class AppExceptions {

    public static class ApiException extends RuntimeException {
        private final HttpStatus status;
        private final String title;

        public ApiException(HttpStatus status, String title, String detail) {
            super(detail);
            this.status = status;
            this.title = title;
        }

        public HttpStatus getStatus() { return status; }
        public String getTitle() { return title; }
    }

    public static class NotFoundException extends ApiException {
        public NotFoundException(String detail) { super(HttpStatus.NOT_FOUND, "Not Found", detail); }
    }

    public static class ValidationException extends ApiException {
        public ValidationException(String detail) { super(HttpStatus.BAD_REQUEST, "Validation Failed", detail); }
    }

    public static class ForbiddenException extends ApiException {
        public ForbiddenException(String detail) { super(HttpStatus.FORBIDDEN, "Forbidden", detail); }
    }

    public static class UnauthorizedException extends ApiException {
        public UnauthorizedException(String detail) { super(HttpStatus.UNAUTHORIZED, "Unauthorized", detail); }
    }

    public static class TooManyRequestsException extends ApiException {
        public TooManyRequestsException(String detail) { super(HttpStatus.TOO_MANY_REQUESTS, "Too Many Requests", detail); }
    }

    private AppExceptions() {}
}
