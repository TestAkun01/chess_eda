package com.zanra.catur.models;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Entity
@Table(name = "moves")
public class Move {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "game_id")
    private Game game;


    private String moveNotation; // misal "e4", "Nf3"

    private LocalDateTime moveTime;

    private String fen;

}
