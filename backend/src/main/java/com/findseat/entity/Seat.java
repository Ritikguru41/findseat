package com.findseat.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.findseat.enums.SeatStatus;
import com.findseat.enums.SeatType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "seats")
@Getter
@Setter
@NoArgsConstructor
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "show_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Show show;

    @JsonProperty("seat_number")
    @Column(name = "seat_number", nullable = false, length = 10)
    private String seatNumber;

    @JsonProperty("row_label")
    @Column(name = "row_label", length = 5)
    private String rowLabel;

    @JsonProperty("seat_num")
    @Column(name = "seat_num")
    private Integer seatNum;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private SeatType type = SeatType.NORMAL;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private SeatStatus status = SeatStatus.AVAILABLE;

    @JsonProperty("locked_by")
    @Column(name = "locked_by")
    private Long lockedBy;

    @JsonProperty("lock_expires")
    @Column(name = "lock_expires")
    private LocalDateTime lockExpires;
}
