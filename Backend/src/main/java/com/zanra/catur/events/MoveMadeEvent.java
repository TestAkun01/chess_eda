package com.zanra.catur.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MoveMadeEvent extends BaseEvent {
    private Long gameId;
    private Long playerId;
    private String move;
    private String from;
    private String to;
    private String promotion;
}