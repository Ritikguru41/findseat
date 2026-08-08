package com.findseat.controller;

import com.findseat.dto.ShowDTO;
import com.findseat.dto.ShowRequest;
import com.findseat.dto.ShowUpdateRequest;
import com.findseat.security.RequireAuth;
import com.findseat.service.ShowService;
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
@RequestMapping("/api/shows")
public class ShowController {

    private final ShowService showService;

    public ShowController(ShowService showService) {
        this.showService = showService;
    }

    @GetMapping
    @RequireAuth(admin = true)
    public List<ShowDTO> getAllShows() {
        return showService.getAllShows();
    }

    @GetMapping("/movie/{movieId}")
    public List<ShowDTO> getShowsByMovie(@PathVariable Long movieId) {
        return showService.getShowsByMovie(movieId);
    }

    @GetMapping("/{id}")
    public ShowDTO getShowById(@PathVariable Long id) {
        return showService.getShowById(id);
    }

    @PostMapping
    @RequireAuth(admin = true)
    public ResponseEntity<Map<String, Object>> create(@Valid @RequestBody ShowRequest request) {
        return ResponseEntity.ok(showService.create(request));
    }

    @PutMapping("/{id}")
    @RequireAuth(admin = true)
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id,
                                                      @Valid @RequestBody ShowUpdateRequest request) {
        return ResponseEntity.ok(showService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @RequireAuth(admin = true)
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        return ResponseEntity.ok(showService.delete(id));
    }
}
