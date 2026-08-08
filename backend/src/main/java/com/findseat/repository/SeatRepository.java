package com.findseat.repository;

import com.findseat.entity.Seat;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByShowIdOrderByRowLabelAscSeatNumAsc(Long showId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Seat s WHERE s.id IN :ids AND s.show.id = :showId")
    List<Seat> findByIdsAndShowForUpdate(@Param("ids") List<Long> ids, @Param("showId") Long showId);

    @Query("SELECT s FROM Seat s WHERE s.show.id = :showId AND s.status = com.findseat.enums.SeatStatus.LOCKED " +
           "AND s.lockExpires < :now")
    List<Seat> findExpiredLocks(@Param("showId") Long showId, @Param("now") java.time.LocalDateTime now);
}
