package com.findseat.controller;

import com.findseat.dto.BookingDTO;
import com.findseat.dto.BookingRequest;
import com.findseat.security.RequireAuth;
import com.findseat.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @RequireAuth
    public ResponseEntity<Map<String, Object>> createBooking(@Valid @RequestBody BookingRequest request,
                                                             @RequestAttribute("userId") Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookingService.createBooking(request, userId));
    }

    @GetMapping("/my")
    @RequireAuth
    public List<BookingDTO> getUserBookings(@RequestAttribute("userId") Long userId) {
        return bookingService.getUserBookings(userId);
    }

    @GetMapping("/all")
    @RequireAuth(admin = true)
    public List<BookingDTO> getAllBookings() {
        return bookingService.getAllBookings();
    }

    @GetMapping("/download/{bookingId}")
    @RequireAuth
    public ResponseEntity<byte[]> downloadTicket(@PathVariable String bookingId,
                                                 @RequestAttribute("userId") Long userId) {
        byte[] pdf = bookingService.downloadTicket(bookingId, userId);
        if (pdf == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"FindSeat_" + bookingId + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/validate/{bookingId}")
    public ResponseEntity<Map<String, Object>> validateTicket(@PathVariable String bookingId) {
        return ResponseEntity.ok(bookingService.validateTicket(bookingId));
    }
}
