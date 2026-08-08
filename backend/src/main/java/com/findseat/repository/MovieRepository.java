package com.findseat.repository;

import com.findseat.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    @Query("SELECT m FROM Movie m " +
           "WHERE (:search = '' OR m.title LIKE CONCAT('%', :search, '%') " +
           "OR m.description LIKE CONCAT('%', :search, '%')) " +
           "AND (:genre = '' OR m.genre = :genre) " +
           "ORDER BY m.createdAt DESC")
    List<Movie> searchMovies(@Param("search") String search, @Param("genre") String genre);

    @Query("SELECT DISTINCT m.genre FROM Movie m WHERE m.genre IS NOT NULL AND m.genre <> ''")
    List<String> findDistinctGenres();
}
