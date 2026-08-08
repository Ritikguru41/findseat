package com.findseat.service;

import com.findseat.dto.BookingDTO;
import com.findseat.dto.BookingRequest;
import com.findseat.dto.BookingResultDTO;
import com.findseat.entity.Booking;
import com.findseat.entity.Seat;
import com.findseat.entity.Show;
import com.findseat.entity.User;
import com.findseat.enums.SeatStatus;
import com.findseat.exception.ApiException;
import com.findseat.exception.ResourceNotFoundException;
import com.findseat.repository.BookingRepository;
import com.findseat.repository.SeatRepository;
import com.findseat.repository.ShowRepository;
import com.findseat.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private final BookingRepository bookingRepository;
    private final SeatRepository seatRepository;
    private final ShowRepository showRepository;
    private final UserRepository userRepository;
    private final PdfService pdfService;
    private final EmailService emailService;

    public BookingService(BookingRepository bookingRepository,
                          SeatRepository seatRepository,
                          ShowRepository showRepository,
                          UserRepository userRepository,
                          PdfService pdfService,
                          EmailService emailService) {
        this.bookingRepository = bookingRepository;
        this.seatRepository = seatRepository;
        this.showRepository = showRepository;
        this.userRepository = userRepository;
        this.pdfService = pdfService;
        this.emailService = emailService;
    }

    @Transactional
    public Map<String, Object> createBooking(BookingRequest request, Long userId) {
        String paymentId = request.getPaymentId();

        if (paymentId != null && !paymentId.startsWith("pay_demo_")
                && bookingRepository.existsByPaymentId(paymentId)) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "This payment has already been used for a booking.");
        }

        List<Seat> seats = seatRepository.findByIdsAndShowForUpdate(request.getSeatIds(), request.getShowId());
        if (seats.size() != request.getSeatIds().size()
                || seats.stream().anyMatch(s -> !userId.equals(s.getLockedBy()))) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Seat lock expired or seats no longer belong to you. Please select seats again.");
        }

        List<String> bookedSeats = seats.stream()
                .filter(s -> s.getStatus() == SeatStatus.BOOKED)
                .map(Seat::getSeatNumber)
                .toList();
        if (!bookedSeats.isEmpty()) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Seats already booked: " + String.join(", ", bookedSeats));
        }

        Show show = showRepository.findById(request.getShowId())
                .orElseThrow(() -> new ResourceNotFoundException("Show not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String seatNumbers = seats.stream().map(Seat::getSeatNumber).toList().toString()
                .replace("[", "").replace("]", "");
        String bookingId = "FS" + String.valueOf(System.currentTimeMillis()).substring(
                String.valueOf(System.currentTimeMillis()).length() - 8);

        Booking booking = new Booking();
        booking.setBookingId(bookingId);
        booking.setUser(user);
        booking.setShow(show);
        booking.setSeatNumbers(seatNumbers);
        booking.setTotalAmount(request.getTotalAmount());
        booking.setPaymentId(paymentId);
        booking.setRazorpayOrderId(request.getRazorpayOrderId());
        bookingRepository.save(booking);

        for (Seat seat : seats) {
            seat.setStatus(SeatStatus.BOOKED);
            seat.setLockedBy(null);
            seat.setLockExpires(null);
        }
        seatRepository.saveAll(seats);

        show.setAvailableSeats(show.getAvailableSeats() - seats.size());
        showRepository.save(show);

        sendTicketAsync(user, show, bookingId, seatNumbers, request.getTotalAmount(), paymentId);

        BookingResultDTO result = new BookingResultDTO(
                booking.getId(),
                bookingId,
                seatNumbers,
                request.getTotalAmount(),
                paymentId,
                show.getShowDate(),
                show.getShowTime(),
                show.getMovie().getTitle(),
                show.getCinema().getName(),
                show.getCinema().getLocation(),
                show.getScreen(),
                show.getMovie().getGenre(),
                show.getMovie().getPosterUrl(),
                user.getName(),
                user.getEmail());

        return Map.of("message", "Booking confirmed!", "booking", result);
    }

    @Transactional(readOnly = true)
    public List<BookingDTO> getUserBookings(Long userId) {
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<BookingDTO> getAllBookings() {
        return bookingRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public byte[] downloadTicket(String bookingId, Long userId) {
        Booking booking = bookingRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        if (!booking.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Booking not found");
        }
        return buildTicketPdf(booking);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> validateTicket(String bookingId) {
        Booking booking = bookingRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        Map.of("valid", false, "message", "Booking not found")));

        boolean valid = booking.getStatus() != null
                && booking.getStatus().name().equalsIgnoreCase("CONFIRMED");

        Map<String, Object> bookingData = new LinkedHashMap<>();
        bookingData.put("bookingId", booking.getBookingId());
        bookingData.put("movieTitle", booking.getShow().getMovie().getTitle());
        bookingData.put("userName", booking.getUser().getName());
        bookingData.put("seats", booking.getSeatNumbers());
        bookingData.put("showDate", booking.getShow().getShowDate());
        bookingData.put("showTime", booking.getShow().getShowTime());
        bookingData.put("screen", booking.getShow().getScreen());
        bookingData.put("cinemaName", booking.getShow().getCinema().getName());
        bookingData.put("status", booking.getStatus().name().toLowerCase());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("valid", valid);
        body.put("booking", bookingData);
        return body;
    }

    private byte[] buildTicketPdf(Booking booking) {
        Show show = booking.getShow();
        String showDate = show.getShowDate() != null ? show.getShowDate().format(DATE_FMT) : "";
        String showTime = show.getShowTime() != null ? show.getShowTime().format(TIME_FMT) : "";

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("bookingId", booking.getBookingId());
        data.put("userId", booking.getUser().getId());
        data.put("showId", show.getId());
        data.put("movieTitle", show.getMovie().getTitle());
        data.put("genre", show.getMovie().getGenre());
        data.put("userName", booking.getUser().getName());
        data.put("seats", booking.getSeatNumbers());
        data.put("showDate", showDate);
        data.put("showTime", showTime);
        data.put("screen", show.getScreen());
        data.put("cinemaName", show.getCinema().getName());
        data.put("cinemaLocation", show.getCinema().getLocation());
        data.put("totalAmount", booking.getTotalAmount());
        data.put("paymentId", booking.getPaymentId());

        try {
            return pdfService.generateTicketPdf(data);
        } catch (Exception ex) {
            log.error("PDF generation failed: {}", ex.getMessage());
            return null;
        }
    }

    private void sendTicketAsync(User user, Show show, String bookingId, String seatNumbers,
                                 Double totalAmount, String paymentId) {
        byte[] pdf = buildTicketPdf(show, bookingId, seatNumbers, totalAmount, paymentId, user);
        if (pdf == null) {
            return;
        }
        try {
            emailService.sendBookingConfirmationEmail(
                    user.getEmail(),
                    user.getName(),
                    show.getMovie().getTitle(),
                    show.getShowDate() != null ? show.getShowDate().format(DATE_FMT) : "",
                    show.getShowTime() != null ? show.getShowTime().format(TIME_FMT) : "",
                    seatNumbers,
                    String.valueOf(totalAmount),
                    bookingId,
                    pdf);
        } catch (Exception ex) {
            log.error("Booking confirmation email failed: {}", ex.getMessage());
        }
    }

    private byte[] buildTicketPdf(Show show, String bookingId, String seatNumbers,
                                  Double totalAmount, String paymentId, User user) {
        String showDate = show.getShowDate() != null ? show.getShowDate().format(DATE_FMT) : "";
        String showTime = show.getShowTime() != null ? show.getShowTime().format(TIME_FMT) : "";

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("bookingId", bookingId);
        data.put("userId", user.getId());
        data.put("showId", show.getId());
        data.put("movieTitle", show.getMovie().getTitle());
        data.put("genre", show.getMovie().getGenre());
        data.put("userName", user.getName());
        data.put("seats", seatNumbers);
        data.put("showDate", showDate);
        data.put("showTime", showTime);
        data.put("screen", show.getScreen());
        data.put("cinemaName", show.getCinema().getName());
        data.put("cinemaLocation", show.getCinema().getLocation());
        data.put("totalAmount", totalAmount);
        data.put("paymentId", paymentId);

        try {
            return pdfService.generateTicketPdf(data);
        } catch (Exception ex) {
            log.error("PDF generation failed: {}", ex.getMessage());
            return null;
        }
    }

    private BookingDTO toDto(Booking booking) {
        BookingDTO dto = new BookingDTO();
        Show show = booking.getShow();
        dto.setId(booking.getId());
        dto.setBookingId(booking.getBookingId());
        dto.setUserId(booking.getUser().getId());
        dto.setShowId(show.getId());
        dto.setSeatNumbers(booking.getSeatNumbers());
        dto.setTotalAmount(booking.getTotalAmount());
        dto.setPaymentId(booking.getPaymentId());
        dto.setRazorpayOrderId(booking.getRazorpayOrderId());
        dto.setStatus(booking.getStatus().name().toLowerCase());
        dto.setCreatedAt(booking.getCreatedAt());
        dto.setMovieTitle(show.getMovie().getTitle());
        dto.setPosterUrl(show.getMovie().getPosterUrl());
        dto.setGenre(show.getMovie().getGenre());
        dto.setShowDate(show.getShowDate());
        dto.setShowTime(show.getShowTime());
        dto.setScreen(show.getScreen());
        dto.setPrice(show.getPrice());
        dto.setCinemaName(show.getCinema().getName());
        dto.setCinemaLocation(show.getCinema().getLocation());
        dto.setUserName(booking.getUser().getName());
        dto.setUserEmail(booking.getUser().getEmail());
        return dto;
    }
}
