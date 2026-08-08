package com.findseat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ShowRequest {

    @JsonProperty("movie_id")
    @NotNull(message = "movie_id is required")
    private Long movieId;

    @JsonProperty("cinema_id")
    @NotNull(message = "cinema_id is required")
    private Long cinemaId;

    @JsonProperty("show_date")
    @NotNull(message = "show_date is required")
    private LocalDate showDate;

    @JsonProperty("show_time")
    @NotNull(message = "show_time is required")
    private LocalTime showTime;

    private String screen = "1";

    @JsonProperty("total_seats")
    private Integer totalSeats = 64;

    private Double price = 200.0;
}
