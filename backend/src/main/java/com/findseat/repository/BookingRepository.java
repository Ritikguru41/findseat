package com.findseat.repository;

import com.findseat.entity.Booking;
import com.findseat.enums.BookingStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT b FROM Booking b JOIN FETCH b.user JOIN FETCH b.show s JOIN FETCH s.movie JOIN FETCH s.cinema WHERE b.user.id = :userId ORDER BY b.createdAt DESC")
    List<Booking> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    @Query("SELECT b FROM Booking b JOIN FETCH b.user JOIN FETCH b.show s JOIN FETCH s.movie JOIN FETCH s.cinema ORDER BY b.createdAt DESC")
    List<Booking> findAllByOrderByCreatedAtDesc();

    Optional<Booking> findByBookingId(String bookingId);

    boolean existsByPaymentId(String paymentId);

    long countByStatus(BookingStatus status);

    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Booking b WHERE b.status = :status")
    Double sumTotalAmountByStatus(@Param("status") BookingStatus status);

    List<Booking> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT m.title, COALESCE(SUM(b.totalAmount), 0), COUNT(b.id) " +
           "FROM Movie m LEFT JOIN m.shows s LEFT JOIN s.bookings b WITH b.status = :status " +
           "GROUP BY m.id, m.title " +
           "ORDER BY COALESCE(SUM(b.totalAmount), 0) DESC")
    List<Object[]> findRevenueByMovie(@Param("status") BookingStatus status, Pageable pageable);
}
