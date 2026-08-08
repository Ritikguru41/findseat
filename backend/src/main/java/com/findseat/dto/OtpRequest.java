package com.findseat.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OtpRequest {

    @NotBlank(message = "Email and OTP required")
    private String email;

    @NotBlank(message = "Email and OTP required")
    private String otp;
}
