package com.findseat.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SeatStatus {
    AVAILABLE, LOCKED, BOOKED;

    @JsonValue
    public String jsonValue() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static SeatStatus from(String value) {
        if (value == null || value.isBlank()) {
            return AVAILABLE;
        }
        return SeatStatus.valueOf(value.trim().toUpperCase());
    }
}
