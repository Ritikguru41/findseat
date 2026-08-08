package com.findseat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidatedBookingDTO {

    private String bookingId;
    private String movieTitle;
    private String userName;
    private String seats;
    private LocalDate showDate;
    private LocalTime showTime;
    private String screen;
    private String cinemaName;
    private String status;
}
