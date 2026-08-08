package com.findseat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ShowUpdateRequest {

    @JsonProperty("show_date")
    private LocalDate showDate;

    @JsonProperty("show_time")
    private LocalTime showTime;

    private String screen;

    private Double price;

    @JsonProperty("cinema_id")
    private Long cinemaId;
}
