package com.findseat.repository;

import com.findseat.entity.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ShowRepository extends JpaRepository<Show, Long> {

    @Query("SELECT s FROM Show s JOIN FETCH s.movie JOIN FETCH s.cinema " +
           "WHERE s.movie.id = :movieId AND s.showDate >= CURRENT_DATE " +
           "ORDER BY s.showDate ASC, s.showTime ASC")
    List<Show> findByMovieIdWithDate(@Param("movieId") Long movieId);

    @Query("SELECT s FROM Show s JOIN FETCH s.movie JOIN FETCH s.cinema ORDER BY s.showDate DESC, s.showTime DESC")
    List<Show> findAllOrderByDateDesc();

    @Query("SELECT COUNT(s) FROM Show s WHERE s.showDate >= CURRENT_DATE")
    long countUpcoming();
}
