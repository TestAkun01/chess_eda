package com.zanra.catur.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class AuthDTO {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        private String username;
        private String password;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private String message;
        private String eventId;
        private Long userId;
        private String username;
        private String token;

        public Response(String message, String eventId) {
            this.message = message;
            this.eventId = eventId;
        }

        public Response(Long userId, String username) {
            this.userId = userId;
            this.username = username;
        }

        public Response(Long userId, String username, String token) {
            this.userId = userId;
            this.username = username;
            this.token = token;
        }
    }
}