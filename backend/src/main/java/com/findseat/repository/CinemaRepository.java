package com.findseat.repository;

import com.findseat.entity.Cinema;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CinemaRepository extends JpaRepository<Cinema, Long> {

    List<Cinema> findAllByOrderByCreatedAtDesc();
}
