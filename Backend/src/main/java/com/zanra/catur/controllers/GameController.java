package com.zanra.catur.controllers;

import com.zanra.catur.dto.GameDTO;
import com.zanra.catur.dto.ResponseDTO;
import com.zanra.catur.events.DisconnectUserFromGameEvent;
import com.zanra.catur.events.MatchMakingRequestEvent;
import com.zanra.catur.events.MoveMadeEvent;
import com.zanra.catur.events.ReconnectUserToGameEvent;
import com.zanra.catur.events.SurenderRequestEvent;
import com.zanra.catur.kafka.producers.GameEventProducer;
import com.zanra.catur.models.Game;
import com.zanra.catur.models.User;
import com.zanra.catur.services.GameService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/game")
public class GameController {

    @Autowired
    private GameService gameService;
    @Autowired
    private GameEventProducer gameEventProducer;

    @PostMapping("/move")
    public ResponseEntity<?> makeMove(@RequestBody GameDTO.MoveRequest requestDTO, HttpServletRequest request) {
        User user = (User) request.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ResponseDTO.error(HttpStatus.UNAUTHORIZED, "Unauthorized"));
        }

        if (requestDTO.getGameId() == null || requestDTO.getFrom() == null || requestDTO.getTo() == null) {
            return ResponseEntity.badRequest().body(ResponseDTO.error(HttpStatus.BAD_REQUEST, "Invalid move data"));
        }

        MoveMadeEvent event = new MoveMadeEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setGameId(requestDTO.getGameId());
        event.setPlayerId(user.getId());
        event.setFrom(requestDTO.getFrom());
        event.setTo(requestDTO.getTo());
        event.setPromotion(requestDTO.getPromotion());

        gameEventProducer.sendMoveMade(event);

        return ResponseEntity.ok(ResponseDTO.success("Move submitted", event.getGameId()));
    }

    @PostMapping("/surrender")
    public ResponseEntity<?> surrender(@RequestBody GameDTO.SurenderRequest requestDTO, HttpServletRequest request) {
        User user = (User) request.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ResponseDTO.error(HttpStatus.UNAUTHORIZED, "Unauthorized"));
        }

        if (requestDTO.getGameId() == null || requestDTO.getPlayerId() == null) {
            return ResponseEntity.badRequest()
                    .body(ResponseDTO.error(HttpStatus.BAD_REQUEST, "Game Id atau Player Id tidak tersedia "));
        }
        SurenderRequestEvent event = new SurenderRequestEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setGameId(requestDTO.getGameId());
        event.setPlayerId(requestDTO.getPlayerId());

        gameEventProducer.sendSurenderRequest(event);
        return ResponseEntity.ok(ResponseDTO.success("Surender Request Submitted", event.getEventId()));
    }

    @PostMapping("/matchmaking/join")
    public ResponseEntity<?> joinMatchmaking(HttpServletRequest request) {
        User user = (User) request.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ResponseDTO.error(HttpStatus.UNAUTHORIZED, "Unauthorized"));
        }

        MatchMakingRequestEvent event = new MatchMakingRequestEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setUserId(user.getId());

        gameEventProducer.sendMatchMakingRequest(event);
        return ResponseEntity.ok(ResponseDTO.success("Joined matchmaking queue", event.getEventId()));
    }

    @GetMapping("/resume")
    public ResponseEntity<?> resumeGame(HttpServletRequest request) {
        User user = (User) request.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ResponseDTO.error(HttpStatus.UNAUTHORIZED, "Unauthorized"));
        }

        Game activeGame = gameService.reconnectUserToGame(user.getId());
        if (activeGame != null) {
            ReconnectUserToGameEvent event = new ReconnectUserToGameEvent(activeGame.getId(), user.getId());
            gameEventProducer.sendReconnectUserToGameRequest(event);
            return ResponseEntity.ok(ResponseDTO.success("Reconnected to active game", Map.of(
                    "hasActiveGame", true,
                    "game", activeGame)));
        } else {
            return ResponseEntity.ok(ResponseDTO.success("No active game found", Map.of(
                    "hasActiveGame", false)));
        }

    }

    @PostMapping("/disconnect")
    public ResponseEntity<?> disconnectFromGame(HttpServletRequest request) {
        User user = (User) request.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ResponseDTO.error(HttpStatus.UNAUTHORIZED, "Unauthorized"));
        }
        Game game = gameService.findActiveGameByUserId(user.getId()).orElse(null);
        if (game == null) {
            return ResponseEntity.badRequest().body(ResponseDTO.error(HttpStatus.BAD_REQUEST, "Game Not Found"));
        }
        DisconnectUserFromGameEvent event = new DisconnectUserFromGameEvent(game.getId(), user.getId());

        gameEventProducer.sendDisconnectUserFromGameRequest(event);
        return ResponseEntity.ok(ResponseDTO.success("Disconnected from game"));

    }
}
