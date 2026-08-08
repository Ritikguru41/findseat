package com.findseat.exception;

import org.springframework.http.HttpStatus;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Payment verification failure. Body matches the old backend: { success: false, message }
 */
public class PaymentVerificationException extends ApiException {

    public PaymentVerificationException(String message) {
        super(HttpStatus.BAD_REQUEST, buildBody(message));
    }

    private static Map<String, Object> buildBody(String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("message", message);
        return body;
    }
}
