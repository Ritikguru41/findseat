package com.findseat.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResendOtpRequest {

    @NotBlank(message = "Email required")
    @Email(message = "Invalid email format")
    private String email;
}
