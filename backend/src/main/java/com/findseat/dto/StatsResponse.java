package com.findseat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatsResponse {

    private Long totalUsers;
    private Long totalBookings;
    private Double totalRevenue;
    private Long totalMovies;
    private Long totalShows;
    private List<RevenueByMovieDTO> revenueByMovie;
    private List<RecentBookingDTO> recentBookings;
}
