package com.findseat.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "Email and password required")
    private String email;

    @NotBlank(message = "Email and password required")
    private String password;
}
