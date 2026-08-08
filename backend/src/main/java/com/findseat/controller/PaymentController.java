package com.findseat.controller;

import com.findseat.dto.CreateOrderRequest;
import com.findseat.dto.VerifyPaymentRequest;
import com.findseat.security.RequireAuth;
import com.findseat.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/create-order")
    @RequireAuth
    public ResponseEntity<Map<String, Object>> createOrder(@Valid @RequestBody CreateOrderRequest request,
                                                           @RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(paymentService.createOrder(request, userId));
    }

    @PostMapping("/verify")
    @RequireAuth
    public ResponseEntity<Map<String, Object>> verifyPayment(@RequestBody VerifyPaymentRequest request) {
        return ResponseEntity.ok(paymentService.verifyPayment(request));
    }
}
