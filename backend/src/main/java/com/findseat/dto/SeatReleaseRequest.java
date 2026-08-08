package com.findseat.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class SeatReleaseRequest {

    @NotEmpty(message = "seatIds are required")
    private List<Long> seatIds;
}
