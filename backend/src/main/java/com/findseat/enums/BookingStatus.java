package com.findseat.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum BookingStatus {
    PENDING, CONFIRMED, CANCELLED;

    @JsonValue
    public String jsonValue() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static BookingStatus from(String value) {
        if (value == null || value.isBlank()) {
            return PENDING;
        }
        return BookingStatus.valueOf(value.trim().toUpperCase());
    }
}
