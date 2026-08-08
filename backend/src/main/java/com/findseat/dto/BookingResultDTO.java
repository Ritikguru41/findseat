package com.findseat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingResultDTO {

    private Long id;
    private String bookingId;
    private String seatNumbers;
    private Double totalAmount;
    private String paymentId;
    private LocalDate showDate;
    private LocalTime showTime;
    private String movieTitle;
    private String cinemaName;
    private String cinemaLocation;
    private String screen;
    private String genre;
    private String posterUrl;
    private String userName;
    private String userEmail;
}
