package com.findseat.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "All fields are required")
    private String name;

    @NotBlank(message = "All fields are required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "All fields are required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
}
