package com.findseat.service;

import com.findseat.dto.CreateOrderRequest;
import com.findseat.dto.VerifyPaymentRequest;
import com.findseat.entity.Seat;
import com.findseat.enums.SeatStatus;
import com.findseat.exception.ApiException;
import com.findseat.exception.BadRequestException;
import com.findseat.exception.PaymentVerificationException;
import com.findseat.repository.SeatRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final SeatRepository seatRepository;
    private final String keyId;
    private final String keySecret;

    public PaymentService(SeatRepository seatRepository,
                          @Value("${razorpay.key.id}") String keyId,
                          @Value("${razorpay.key.secret}") String keySecret) {
        this.seatRepository = seatRepository;
        this.keyId = keyId;
        this.keySecret = keySecret;
    }

    private boolean hasValidCredentials() {
        boolean validKey = keyId != null && (keyId.startsWith("rzp_test_") || keyId.startsWith("rzp_live_"));
        boolean validSecret = keySecret != null && keySecret.length() > 10;
        if (!validKey || !validSecret) {
            log.warn("[Razorpay] Missing or invalid credentials - running in demo mode");
        }
        return validKey && validSecret;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> createOrder(CreateOrderRequest request, Long userId) {
        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw new BadRequestException("Invalid amount");
        }

        List<Seat> lockedSeats = seatRepository.findByIdsAndShowForUpdate(request.getSeatIds(), request.getShowId());
        boolean allLocked = lockedSeats.size() == request.getSeatIds().size()
                && lockedSeats.stream().allMatch(s ->
                        s.getStatus() == SeatStatus.LOCKED && userId.equals(s.getLockedBy()));
        if (!allLocked) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "One or more seats are no longer locked. Please select your seats again.");
        }

        if (!hasValidCredentials()) {
            Map<String, Object> demo = new LinkedHashMap<>();
            demo.put("orderId", "order_demo_" + System.currentTimeMillis());
            demo.put("amount", Math.round(request.getAmount() * 100));
            demo.put("currency", "INR");
            demo.put("key", "demo_key");
            demo.put("isDemo", true);
            return demo;
        }

        try {
            RazorpayClient client = new RazorpayClient(keyId, keySecret);
            JSONObject options = new JSONObject();
            options.put("amount", Math.round(request.getAmount() * 100));
            options.put("currency", "INR");
            options.put("receipt", "FS_" + userId + "_" + System.currentTimeMillis());
            options.put("notes", new JSONObject()
                    .put("userId", userId)
                    .put("showId", request.getShowId())
                    .put("seats", String.join(",", request.getSeatIds().stream().map(String::valueOf).toList())));

            Order order = client.orders.create(options);

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("orderId", order.get("id"));
            body.put("amount", order.get("amount"));
            body.put("currency", order.get("currency"));
            body.put("key", keyId);
            body.put("isDemo", false);
            return body;
        } catch (Exception ex) {
            log.error("[createOrder] ERROR: {}", ex.getMessage());
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ex.getMessage() != null ? ex.getMessage() : "Failed to create payment order");
        }
    }

    public Map<String, Object> verifyPayment(VerifyPaymentRequest request) {
        String orderId = request.getRazorpayOrderId();
        String paymentId = request.getRazorpayPaymentId();
        String signature = request.getRazorpaySignature();

        boolean isDemo = Boolean.TRUE.equals(request.getIsDemo())
                || (orderId != null && orderId.startsWith("order_demo_"));

        if (isDemo) {
            if (hasValidCredentials()) {
                throw new PaymentVerificationException("Payment verification failed");
            }
            return Map.of("success", true, "paymentId", "pay_demo_" + System.currentTimeMillis());
        }

        if (orderId == null || paymentId == null || signature == null) {
            throw new PaymentVerificationException("Missing payment fields");
        }

        String body = orderId + "|" + paymentId;
        String expected = hmacSha256(body, keySecret);
        if (!MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                signature.getBytes(StandardCharsets.UTF_8))) {
            log.warn("[verifyPayment] Signature mismatch for order: {}", orderId);
            throw new PaymentVerificationException("Payment verification failed. Invalid signature.");
        }

        return Map.of("success", true, "paymentId", paymentId);
    }

    private String hmacSha256(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("HMAC computation failed", ex);
        }
    }
}
