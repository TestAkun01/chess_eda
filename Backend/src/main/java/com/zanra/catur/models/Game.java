package com.zanra.catur.models;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "games")
public class Game {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "player_white_id")
    private User playerWhite;

    @ManyToOne
    @JoinColumn(name = "player_black_id")
    private User playerBlack;

    @Column(length = 1000)
    private String fen;

    @Enumerated(EnumType.STRING)
    private GameStatus status;

    @Enumerated(EnumType.STRING)
    private FinishReason finishReason;

    @ManyToOne
    @JoinColumn(name = "winner_id")
    private User winner;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @PrePersist
    public void prePersist() {
        if (startTime == null) {
            startTime = LocalDateTime.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        if (GameStatus.FINISHED.equals(status) && endTime == null) {
            endTime = LocalDateTime.now();
        }
    }

    public enum GameStatus {
        ONGOING,
        FINISHED,
        ABANDONED,
        DRAW
    }

    public enum FinishReason {
        SURRENDER,
        TIMEOUT,
        CHECKMATE,
        DISCONNECT,
        DRAW_AGREEMENT,
        STALEMATE
    }
}
