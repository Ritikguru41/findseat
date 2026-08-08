package com.findseat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
public class BookingDTO {

    private Long id;

    @JsonProperty("booking_id")
    private String bookingId;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("show_id")
    private Long showId;

    @JsonProperty("seat_numbers")
    private String seatNumbers;

    @JsonProperty("total_amount")
    private Double totalAmount;

    @JsonProperty("payment_id")
    private String paymentId;

    @JsonProperty("razorpay_order_id")
    private String razorpayOrderId;

    private String status;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("movie_title")
    private String movieTitle;

    @JsonProperty("poster_url")
    private String posterUrl;

    private String genre;

    @JsonProperty("show_date")
    private LocalDate showDate;

    @JsonProperty("show_time")
    private LocalTime showTime;

    private String screen;

    private Double price;

    @JsonProperty("cinema_name")
    private String cinemaName;

    @JsonProperty("cinema_location")
    private String cinemaLocation;

    @JsonProperty("user_name")
    private String userName;

    @JsonProperty("user_email")
    private String userEmail;
}
