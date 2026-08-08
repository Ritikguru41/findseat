package com.findseat.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Role {
    USER, ADMIN;

    @JsonValue
    public String jsonValue() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static Role from(String value) {
        if (value == null || value.isBlank()) {
            return USER;
        }
        return Role.valueOf(value.trim().toUpperCase());
    }
}
