package com.findseat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecentBookingDTO {

    @JsonProperty("booking_id")
    private String bookingId;

    @JsonProperty("user_name")
    private String userName;

    @JsonProperty("movie_title")
    private String movieTitle;

    @JsonProperty("total_amount")
    private Double totalAmount;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    private String status;
}
