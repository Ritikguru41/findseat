package com.findseat.controller;

import com.findseat.dto.SeatLockRequest;
import com.findseat.dto.SeatReleaseRequest;
import com.findseat.entity.Seat;
import com.findseat.security.RequireAuth;
import com.findseat.service.SeatService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/seats")
public class SeatController {

    private final SeatService seatService;

    public SeatController(SeatService seatService) {
        this.seatService = seatService;
    }

    @GetMapping("/show/{showId}")
    public List<Seat> getSeatsByShow(@PathVariable Long showId) {
        return seatService.getSeatsByShow(showId);
    }

    @PostMapping("/lock")
    @RequireAuth
    public ResponseEntity<Map<String, Object>> lockSeats(@Valid @RequestBody SeatLockRequest request,
                                                         @RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(seatService.lockSeats(request, userId));
    }

    @PostMapping("/release")
    @RequireAuth
    public ResponseEntity<Map<String, Object>> releaseSeats(@Valid @RequestBody SeatReleaseRequest request,
                                                            @RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(seatService.releaseSeats(request, userId));
    }
}
