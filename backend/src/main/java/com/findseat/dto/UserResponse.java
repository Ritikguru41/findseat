package com.findseat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private String role;

    @JsonProperty("is_verified")
    private boolean verified;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}
