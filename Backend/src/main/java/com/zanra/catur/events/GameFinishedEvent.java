package com.zanra.catur.events;

import com.zanra.catur.models.Game;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GameFinishedEvent {
    private String eventId;
    private Long gameId;
    private Long winnerId;
    private Game.FinishReason reason;
}