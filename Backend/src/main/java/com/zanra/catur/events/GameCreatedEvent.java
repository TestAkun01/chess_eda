package com.zanra.catur.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GameCreatedEvent extends BaseEvent {
    private Long whitePlayerId;
    private Long blackPlayerId;
}