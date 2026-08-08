package com.findseat.service;

import com.findseat.dto.ShowDTO;
import com.findseat.dto.ShowRequest;
import com.findseat.dto.ShowUpdateRequest;
import com.findseat.entity.Cinema;
import com.findseat.entity.Movie;
import com.findseat.entity.Seat;
import com.findseat.entity.Show;
import com.findseat.enums.SeatStatus;
import com.findseat.enums.SeatType;
import com.findseat.exception.ResourceNotFoundException;
import com.findseat.repository.CinemaRepository;
import com.findseat.repository.MovieRepository;
import com.findseat.repository.SeatRepository;
import com.findseat.repository.ShowRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ShowService {

    private static final String[] ROW_LABELS = {"A", "B", "C", "D", "E", "F", "G", "H"};

    private final ShowRepository showRepository;
    private final MovieRepository movieRepository;
    private final CinemaRepository cinemaRepository;
    private final SeatRepository seatRepository;

    public ShowService(ShowRepository showRepository,
                       MovieRepository movieRepository,
                       CinemaRepository cinemaRepository,
                       SeatRepository seatRepository) {
        this.showRepository = showRepository;
        this.movieRepository = movieRepository;
        this.cinemaRepository = cinemaRepository;
        this.seatRepository = seatRepository;
    }

    @Transactional(readOnly = true)
    public List<ShowDTO> getShowsByMovie(Long movieId) {
        return showRepository.findByMovieIdWithDate(movieId)
                .stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public ShowDTO getShowById(Long id) {
        Show show = showRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Show not found"));
        return toDto(show);
    }

    @Transactional(readOnly = true)
    public List<ShowDTO> getAllShows() {
        return showRepository.findAllOrderByDateDesc()
                .stream().map(this::toDto).toList();
    }

    @Transactional
    public Map<String, Object> create(ShowRequest request) {
        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found"));
        Cinema cinema = cinemaRepository.findById(request.getCinemaId())
                .orElseThrow(() -> new ResourceNotFoundException("Cinema not found"));

        int totalSeats = request.getTotalSeats() == null ? 64 : request.getTotalSeats();

        Show show = new Show();
        show.setMovie(movie);
        show.setCinema(cinema);
        show.setShowDate(request.getShowDate());
        show.setShowTime(request.getShowTime());
        show.setScreen(request.getScreen() == null ? "1" : request.getScreen());
        show.setTotalSeats(totalSeats);
        show.setAvailableSeats(totalSeats);
        show.setPrice(request.getPrice() == null ? 200.0 : request.getPrice());
        showRepository.save(show);

        generateSeats(show, totalSeats);
        return Map.of("message", "Show created with seats", "id", show.getId());
    }

    @Transactional
    public Map<String, Object> update(Long id, ShowUpdateRequest request) {
        Show show = showRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Show not found"));
        if (request.getShowDate() != null) {
            show.setShowDate(request.getShowDate());
        }
        if (request.getShowTime() != null) {
            show.setShowTime(request.getShowTime());
        }
        if (request.getScreen() != null) {
            show.setScreen(request.getScreen());
        }
        if (request.getPrice() != null) {
            show.setPrice(request.getPrice());
        }
        if (request.getCinemaId() != null) {
            Cinema cinema = cinemaRepository.findById(request.getCinemaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cinema not found"));
            show.setCinema(cinema);
        }
        showRepository.save(show);
        return Map.of("message", "Show updated");
    }

    @Transactional
    public Map<String, Object> delete(Long id) {
        showRepository.deleteById(id);
        return Map.of("message", "Show deleted");
    }

    /**
     * Auto-creates seats for a show: 8 rows (A-H), last two rows premium.
     * Mirrors the old Node backend seat generation.
     */
    public void generateSeats(Show show, int totalSeats) {
        int seatsPerRow = (int) Math.ceil(totalSeats / (double) ROW_LABELS.length);
        List<Seat> seats = new ArrayList<>();
        for (int i = 0; i < ROW_LABELS.length; i++) {
            SeatType type = (i >= ROW_LABELS.length - 2) ? SeatType.PREMIUM : SeatType.NORMAL;
            for (int n = 1; n <= seatsPerRow; n++) {
                Seat seat = new Seat();
                seat.setShow(show);
                seat.setSeatNumber(ROW_LABELS[i] + n);
                seat.setRowLabel(ROW_LABELS[i]);
                seat.setSeatNum(n);
                seat.setType(type);
                seat.setStatus(SeatStatus.AVAILABLE);
                seats.add(seat);
            }
        }
        seatRepository.saveAll(seats);
    }

    private ShowDTO toDto(Show show) {
        return new ShowDTO(
                show.getId(),
                show.getMovie().getId(),
                show.getCinema().getId(),
                show.getShowDate(),
                show.getShowTime(),
                show.getScreen(),
                show.getTotalSeats(),
                show.getAvailableSeats(),
                show.getPrice(),
                show.getCreatedAt(),
                show.getMovie().getTitle(),
                show.getCinema().getName(),
                show.getCinema().getLocation(),
                show.getMovie().getPosterUrl(),
                show.getMovie().getDuration(),
                show.getMovie().getGenre());
    }
}
