package com.findseat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MovieRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;
    private String genre;
    private Integer duration;
    private String language = "Hindi";
    private Double rating = 0.0;

    @JsonProperty("poster_url")
    private String posterUrl;

    @JsonProperty("trailer_url")
    private String trailerUrl;

    @JsonProperty("release_date")
    private LocalDate releaseDate;
}
