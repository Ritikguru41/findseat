package com.findseat.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Value;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    private final String keyId;
    private final String keySecret;

    public HealthController(@Value("${razorpay.key.id}") String keyId,
                            @Value("${razorpay.key.secret}") String keySecret) {
        this.keyId = keyId;
        this.keySecret = keySecret;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("status", "OK", "message", "FindSeat API is running");
    }

    @GetMapping("/debug/razorpay")
    public Map<String, Object> razorpayDebug() {
        boolean validKey = keyId != null && (keyId.startsWith("rzp_test_") || keyId.startsWith("rzp_live_"));
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("keyLoaded", keyId != null);
        body.put("keyPrefix", keyId != null ? keyId.substring(0, Math.min(16, keyId.length())) + "..." : "MISSING");
        body.put("secretLoaded", keySecret != null && keySecret.length() > 10);
        body.put("mode", validKey ? "REAL_RAZORPAY" : "DEMO");
        return body;
    }
}
