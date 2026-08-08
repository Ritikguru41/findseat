package com.findseat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class VerifyPaymentRequest {

    @JsonProperty("razorpay_order_id")
    private String razorpayOrderId;

    @JsonProperty("razorpay_payment_id")
    private String razorpayPaymentId;

    @JsonProperty("razorpay_signature")
    private String razorpaySignature;

    private Boolean isDemo;
}
