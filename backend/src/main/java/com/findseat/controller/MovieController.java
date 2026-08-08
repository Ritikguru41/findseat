package com.findseat.controller;

import com.findseat.dto.MovieRequest;
import com.findseat.entity.Movie;
import com.findseat.security.RequireAuth;
import com.findseat.service.MovieService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping
    public List<Movie> getAll(@RequestParam(required = false) String search,
                              @RequestParam(required = false) String genre) {
        return movieService.getAll(search, genre);
    }

    @GetMapping("/genres")
    public List<String> getGenres() {
        return movieService.getGenres();
    }

    @GetMapping("/{id}")
    public Movie getById(@PathVariable Long id) {
        return movieService.getById(id);
    }

    @PostMapping
    @RequireAuth(admin = true)
    public ResponseEntity<Map<String, Object>> create(@Valid @RequestBody MovieRequest request) {
        return ResponseEntity.ok(movieService.create(request));
    }

    @PutMapping("/{id}")
    @RequireAuth(admin = true)
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id,
                                                      @Valid @RequestBody MovieRequest request) {
        return ResponseEntity.ok(movieService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @RequireAuth(admin = true)
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        return ResponseEntity.ok(movieService.delete(id));
    }
}
