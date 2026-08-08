package com.findseat.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Base API exception. Carries an HTTP status and a JSON response body so the
 * global handler can return exactly the shape the React frontend expects.
 */
@Getter
public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final Map<String, Object> body;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
        this.body = new LinkedHashMap<>();
        this.body.put("message", message);
    }

    public ApiException(HttpStatus status, Map<String, Object> body) {
        super(String.valueOf(body.get("message")));
        this.status = status;
        this.body = body;
    }
}
