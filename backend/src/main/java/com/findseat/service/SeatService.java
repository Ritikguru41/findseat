package com.findseat.service;

import com.findseat.dto.SeatLockRequest;
import com.findseat.dto.SeatReleaseRequest;
import com.findseat.entity.Seat;
import com.findseat.enums.SeatStatus;
import com.findseat.exception.SeatLockException;
import com.findseat.repository.SeatRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SeatService {

    private final SeatRepository seatRepository;
    private final int lockMinutes;

    public SeatService(SeatRepository seatRepository,
                       @Value("${app.seat-lock-minutes:5}") int lockMinutes) {
        this.seatRepository = seatRepository;
        this.lockMinutes = lockMinutes;
    }

    @Transactional(readOnly = true)
    public List<Seat> getSeatsByShow(Long showId) {
        return seatRepository.findByShowIdOrderByRowLabelAscSeatNumAsc(showId);
    }

    @Transactional
    public Map<String, Object> lockSeats(SeatLockRequest request, Long userId) {
        List<Seat> seats = seatRepository.findByIdsAndShowForUpdate(request.getSeatIds(), request.getShowId());

        List<String> unavailable = seats.stream()
                .filter(s -> s.getStatus() != SeatStatus.AVAILABLE)
                .map(Seat::getSeatNumber)
                .toList();
        if (!unavailable.isEmpty()) {
            throw new SeatLockException("Some seats are already booked or locked", unavailable);
        }

        LocalDateTime lockExpiry = LocalDateTime.now().plusMinutes(lockMinutes);
        for (Seat seat : seats) {
            seat.setStatus(SeatStatus.LOCKED);
            seat.setLockedBy(userId);
            seat.setLockExpires(lockExpiry);
        }
        seatRepository.saveAll(seats);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "Seats locked for " + lockMinutes + " minutes");
        body.put("lockExpiry", lockExpiry);
        return body;
    }

    @Transactional
    public Map<String, Object> releaseSeats(SeatReleaseRequest request, Long userId) {
        List<Seat> seats = seatRepository.findAllById(request.getSeatIds());
        for (Seat seat : seats) {
            if (userId.equals(seat.getLockedBy())) {
                seat.setStatus(SeatStatus.AVAILABLE);
                seat.setLockedBy(null);
                seat.setLockExpires(null);
            }
        }
        seatRepository.saveAll(seats);
        return Map.of("message", "Seats released");
    }
}
