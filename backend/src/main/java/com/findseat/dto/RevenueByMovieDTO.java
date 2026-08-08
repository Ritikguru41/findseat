package com.findseat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevenueByMovieDTO {

    private String title;
    private Double revenue;
    private Long bookings;
}
