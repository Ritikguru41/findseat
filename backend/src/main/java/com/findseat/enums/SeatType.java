package com.findseat.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SeatType {
    NORMAL, PREMIUM;

    @JsonValue
    public String jsonValue() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static SeatType from(String value) {
        if (value == null || value.isBlank()) {
            return NORMAL;
        }
        return SeatType.valueOf(value.trim().toUpperCase());
    }
}
