package com.zanra.catur.models;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "moves")
public class Move {

    @Id
    @GeneratedValue
    private Long id;

    public Long getId() {
        return id;
    }

    @ManyToOne
    @JoinColumn(name = "game_id")
    private Game game;


    private String moveNotation; // misal "e4", "Nf3"

    private LocalDateTime moveTime;

    private String fen;

    public String getMoveNotation() {
        return moveNotation;
    }

    public LocalDateTime getMoveTime() {
        return moveTime;
    }

    public Game getGame() {
        return game;
    }

    public String getFenAfterMove() {
        return fen;
    }

    public void setGame(Game game2) {
        game = game2;
    }

    public void setMoveNotation(String moveNotation2) {
        moveNotation = moveNotation2;
    }

    public void setFen(String fen) {
        this.fen = fen;
    }

    public void setMoveTime(LocalDateTime now) {
        moveTime = now;
    }
}
