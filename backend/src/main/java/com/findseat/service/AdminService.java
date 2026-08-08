package com.findseat.service;

import com.findseat.dto.RecentBookingDTO;
import com.findseat.dto.RevenueByMovieDTO;
import com.findseat.dto.StatsResponse;
import com.findseat.dto.UserResponse;
import com.findseat.entity.Booking;
import com.findseat.entity.User;
import com.findseat.enums.BookingStatus;
import com.findseat.enums.Role;
import com.findseat.exception.BadRequestException;
import com.findseat.repository.BookingRepository;
import com.findseat.repository.MovieRepository;
import com.findseat.repository.ShowRepository;
import com.findseat.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final MovieRepository movieRepository;
    private final ShowRepository showRepository;

    public AdminService(UserRepository userRepository,
                        BookingRepository bookingRepository,
                        MovieRepository movieRepository,
                        ShowRepository showRepository) {
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.movieRepository = movieRepository;
        this.showRepository = showRepository;
    }

    @Transactional(readOnly = true)
    public StatsResponse getStats() {
        Long totalUsers = userRepository.countByRole(Role.USER);
        Long totalBookings = bookingRepository.countByStatus(BookingStatus.CONFIRMED);
        Double totalRevenue = bookingRepository.sumTotalAmountByStatus(BookingStatus.CONFIRMED);
        Long totalMovies = movieRepository.count();
        Long totalShows = showRepository.countUpcoming();

        Pageable top5 = PageRequest.of(0, 5);
        List<RevenueByMovieDTO> revenueByMovie = new ArrayList<>();
        for (Object[] row : bookingRepository.findRevenueByMovie(BookingStatus.CONFIRMED, top5)) {
            String title = row[0] != null ? row[0].toString() : "Unknown";
            Double revenue = ((Number) row[1]).doubleValue();
            Long bookings = ((Number) row[2]).longValue();
            revenueByMovie.add(new RevenueByMovieDTO(title, revenue, bookings));
        }

        Pageable top10 = PageRequest.of(0, 10);
        List<RecentBookingDTO> recentBookings = bookingRepository
                .findAllByOrderByCreatedAtDesc(top10)
                .stream().map(this::toRecentBooking).toList();

        return new StatsResponse(totalUsers, totalBookings, totalRevenue == null ? 0.0 : totalRevenue,
                totalMovies, totalShows, revenueByMovie, recentBookings);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(this::toUserResponse).toList();
    }

    @Transactional
    public void deleteUser(Long id, Long currentUserId) {
        if (id.equals(currentUserId)) {
            throw new BadRequestException("Cannot delete yourself");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("User not found"));
        if (user.getRole() == Role.ADMIN) {
            throw new BadRequestException("Cannot delete another admin account");
        }
        userRepository.delete(user);
    }

    private RecentBookingDTO toRecentBooking(Booking booking) {
        return new RecentBookingDTO(
                booking.getBookingId(),
                booking.getUser().getName(),
                booking.getShow().getMovie().getTitle(),
                booking.getTotalAmount(),
                booking.getCreatedAt(),
                booking.getStatus().name().toLowerCase());
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name().toLowerCase(),
                user.isVerified(),
                user.getCreatedAt());
    }
}
