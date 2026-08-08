package com.findseat.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {

    @NotNull(message = "Invalid amount")
    private Double amount;

    @NotNull(message = "showId and seatIds are required")
    private Long showId;

    @NotEmpty(message = "showId and seatIds are required")
    private List<Long> seatIds;
}
