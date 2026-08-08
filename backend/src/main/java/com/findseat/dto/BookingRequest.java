package com.findseat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class BookingRequest {

    @NotNull(message = "showId is required")
    private Long showId;

    @NotEmpty(message = "seatIds are required")
    private List<Long> seatIds;

    private String paymentId;

    @JsonProperty("razorpay_order_id")
    private String razorpayOrderId;

    @NotNull(message = "totalAmount is required")
    private Double totalAmount;
}
