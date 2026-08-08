package com.findseat.controller;

import com.findseat.dto.CinemaRequest;
import com.findseat.entity.Cinema;
import com.findseat.security.RequireAuth;
import com.findseat.service.CinemaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cinemas")
public class CinemaController {

    private final CinemaService cinemaService;

    public CinemaController(CinemaService cinemaService) {
        this.cinemaService = cinemaService;
    }

    @GetMapping
    public List<Cinema> getAll() {
        return cinemaService.getAll();
    }

    @PostMapping
    @RequireAuth(admin = true)
    public ResponseEntity<Map<String, Object>> create(@Valid @RequestBody CinemaRequest request) {
        return ResponseEntity.ok(cinemaService.create(request));
    }

    @PutMapping("/{id}")
    @RequireAuth(admin = true)
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id,
                                                      @Valid @RequestBody CinemaRequest request) {
        return ResponseEntity.ok(cinemaService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @RequireAuth(admin = true)
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        return ResponseEntity.ok(cinemaService.delete(id));
    }
}
