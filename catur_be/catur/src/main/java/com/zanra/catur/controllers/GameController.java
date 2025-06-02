package com.zanra.catur.controllers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zanra.catur.dto.ChessMoveDTO;
import com.zanra.catur.dto.CreateGameRequest;
import com.zanra.catur.kafka.producers.ChessMoveProducer;
import com.zanra.catur.models.Game;
import com.zanra.catur.models.User;
import com.zanra.catur.repositories.GameRepository;
import com.zanra.catur.repositories.UserRepository;
import com.zanra.catur.services.GameService;

@RestController
@RequestMapping("/api/game")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:2424"}, allowCredentials="true")
public class GameController {

    @Autowired
    private GameService gameService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private ChessMoveProducer chessMoveProducer;

    @PostMapping("/create")
    public ResponseEntity<?> createGame(@RequestBody CreateGameRequest request) {
        Optional<User> white = userRepository.findById(request.getWhitePlayerId());
        Optional<User> black = userRepository.findById(request.getBlackPlayerId());

        if (white.isEmpty() || black.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("404");
        }

        Game game = gameService.createGame(white.get(), black.get());

        gameService.updateStatusUser(game.getId(), white.get());
        gameService.updateStatusUser(game.getId(), black.get());

        return ResponseEntity.ok(game);
    }

    @PostMapping("/{gameId}/move")
    public ResponseEntity<?> makeMove(
            @PathVariable Long gameId,
            @RequestBody ChessMoveDTO request
    ) {
        Optional<Game> game = gameRepository.findById(gameId);

        if (game.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("404");
        }

        chessMoveProducer.sendMove(gameId, request.getFen());

        gameService.saveMove(game.get(), request.getMoveNotation(), request.getFen());
        return ResponseEntity.ok("Move saved successfully");
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<?> getGame(@PathVariable Long gameId) {
        Optional<Game> optGame = gameRepository.findById(gameId);
        if (optGame.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("404");
        }

        return ResponseEntity.ok(optGame.get());
    }
}
