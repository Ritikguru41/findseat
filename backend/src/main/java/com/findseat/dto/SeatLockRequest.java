package com.findseat.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SeatLockRequest {

    @NotNull(message = "showId is required")
    private Long showId;

    @NotEmpty(message = "seatIds are required")
    private List<Long> seatIds;
}
