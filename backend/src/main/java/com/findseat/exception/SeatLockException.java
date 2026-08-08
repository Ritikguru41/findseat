package com.findseat.exception;

import org.springframework.http.HttpStatus;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Thrown when one or more requested seats are already locked/booked.
 * The body matches the old backend: { message, seats: [...] }
 */
public class SeatLockException extends ApiException {

    public SeatLockException(String message, List<String> seats) {
        super(HttpStatus.CONFLICT, buildBody(message, seats));
    }

    private static Map<String, Object> buildBody(String message, List<String> seats) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", message);
        body.put("seats", seats);
        return body;
    }
}
