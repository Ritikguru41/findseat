package com.findseat.service;

import com.findseat.dto.CinemaRequest;
import com.findseat.entity.Cinema;
import com.findseat.exception.ResourceNotFoundException;
import com.findseat.repository.CinemaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class CinemaService {

    private final CinemaRepository cinemaRepository;

    public CinemaService(CinemaRepository cinemaRepository) {
        this.cinemaRepository = cinemaRepository;
    }

    @Transactional(readOnly = true)
    public List<Cinema> getAll() {
        return cinemaRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public Map<String, Object> create(CinemaRequest request) {
        Cinema cinema = new Cinema();
        copyFields(request, cinema);
        cinemaRepository.save(cinema);
        return Map.of("message", "Cinema added successfully", "id", cinema.getId());
    }

    @Transactional
    public Map<String, Object> update(Long id, CinemaRequest request) {
        Cinema cinema = cinemaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cinema not found"));
        copyFields(request, cinema);
        cinemaRepository.save(cinema);
        return Map.of("message", "Cinema updated successfully");
    }

    @Transactional
    public Map<String, Object> delete(Long id) {
        cinemaRepository.deleteById(id);
        return Map.of("message", "Cinema deleted successfully");
    }

    private void copyFields(CinemaRequest req, Cinema cinema) {
        cinema.setName(req.getName());
        cinema.setLocation(req.getLocation());
        cinema.setAddress(req.getAddress());
        cinema.setTotalScreens(req.getTotalScreens() == null ? 1 : req.getTotalScreens());
    }
}
