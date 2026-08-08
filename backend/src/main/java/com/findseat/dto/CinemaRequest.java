package com.findseat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CinemaRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Location is required")
    private String location;

    private String address;

    @JsonProperty("total_screens")
    private Integer totalScreens = 1;
}
