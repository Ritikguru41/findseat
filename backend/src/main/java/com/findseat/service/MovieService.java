package com.findseat.service;

import com.findseat.dto.MovieRequest;
import com.findseat.entity.Movie;
import com.findseat.exception.ResourceNotFoundException;
import com.findseat.repository.MovieRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class MovieService {

    private final MovieRepository movieRepository;

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    @Transactional(readOnly = true)
    public List<Movie> getAll(String search, String genre) {
        return movieRepository.searchMovies(
                search == null ? "" : search.trim(),
                genre == null ? "" : genre.trim());
    }

    @Transactional(readOnly = true)
    public Movie getById(Long id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found"));
    }

    @Transactional(readOnly = true)
    public List<String> getGenres() {
        return movieRepository.findDistinctGenres();
    }

    @Transactional
    public Map<String, Object> create(MovieRequest request) {
        Movie movie = new Movie();
        copyFields(request, movie);
        movieRepository.save(movie);
        return Map.of("message", "Movie created", "id", movie.getId());
    }

    @Transactional
    public Map<String, Object> update(Long id, MovieRequest request) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found"));
        copyFields(request, movie);
        movieRepository.save(movie);
        return Map.of("message", "Movie updated");
    }

    @Transactional
    public Map<String, Object> delete(Long id) {
        movieRepository.deleteById(id);
        return Map.of("message", "Movie deleted");
    }

    private void copyFields(MovieRequest req, Movie movie) {
        movie.setTitle(req.getTitle());
        movie.setDescription(req.getDescription());
        movie.setGenre(req.getGenre());
        movie.setDuration(req.getDuration());
        movie.setLanguage(req.getLanguage() == null ? "Hindi" : req.getLanguage());
        movie.setRating(req.getRating() == null ? 0.0 : req.getRating());
        movie.setPosterUrl(req.getPosterUrl());
        movie.setTrailerUrl(req.getTrailerUrl());
        movie.setReleaseDate(req.getReleaseDate());
    }
}
