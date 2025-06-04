package com.zanra.catur.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class GameDTO {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        private Long whitePlayerId;
        private Long blackPlayerId;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MoveRequest {
        private Long gameId;
        private String from;
        private String to;
        private String promotion;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SurenderRequest {
        private Long gameId;
        private Long playerId;
    }

}