package com.findseat.exception;

import org.springframework.http.HttpStatus;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Thrown when a user tries to login with an unverified email.
 * The body matches the old backend: { message, unverified: true }
 */
public class UnverifiedEmailException extends ApiException {

    public UnverifiedEmailException(String message) {
        super(HttpStatus.FORBIDDEN, buildBody(message));
    }

    private static Map<String, Object> buildBody(String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", message);
        body.put("unverified", true);
        return body;
    }
}
