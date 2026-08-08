package com.findseat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShowDTO {

    private Long id;

    @JsonProperty("movie_id")
    private Long movieId;

    @JsonProperty("cinema_id")
    private Long cinemaId;

    @JsonProperty("show_date")
    private LocalDate showDate;

    @JsonProperty("show_time")
    private LocalTime showTime;

    private String screen;

    @JsonProperty("total_seats")
    private Integer totalSeats;

    @JsonProperty("available_seats")
    private Integer availableSeats;

    private Double price;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("movie_title")
    private String movieTitle;

    @JsonProperty("cinema_name")
    private String cinemaName;

    @JsonProperty("cinema_location")
    private String cinemaLocation;

    @JsonProperty("poster_url")
    private String posterUrl;

    private Integer duration;

    private String genre;
}
