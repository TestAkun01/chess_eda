package com.zanra.catur.models;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

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

    private String status;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    public User getPlayerWhite() {
        return playerWhite;
    }

    public User getPlayerBlack() {
        return playerBlack;
    }

    public Long getId() {
        return id;
    }

    public String getFen() {
        return fen;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setPlayerWhite(User white) {
        playerWhite = white;
    }

    public void setPlayerBlack(User black) {
        playerBlack = black;
    }

    public void setFen(String newFen) {
        this.fen = newFen;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setStartTime(LocalDateTime now) {
        startTime = now;
    }
}

