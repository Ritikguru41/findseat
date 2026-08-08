package com.findseat.controller;

import com.findseat.dto.LoginRequest;
import com.findseat.dto.OtpRequest;
import com.findseat.dto.RegisterRequest;
import com.findseat.dto.ResendOtpRequest;
import com.findseat.dto.UserResponse;
import com.findseat.security.RequireAuth;
import com.findseat.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, Object>> verifyOtp(@Valid @RequestBody OtpRequest request) {
        return ResponseEntity.ok(authService.verifyOtp(request));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<Map<String, Object>> resendOtp(@Valid @RequestBody ResendOtpRequest request) {
        return ResponseEntity.ok(authService.resendOtp(request));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/profile")
    @RequireAuth
    public ResponseEntity<UserResponse> profile(@RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(authService.getProfile(userId));
    }
}
