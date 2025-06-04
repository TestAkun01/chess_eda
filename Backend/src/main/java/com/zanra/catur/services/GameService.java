package com.zanra.catur.services;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zanra.catur.models.Game;
import com.zanra.catur.models.Move;
import com.zanra.catur.models.User;
import com.zanra.catur.repositories.GameRepository;
import com.zanra.catur.repositories.MoveRepository;

@Service
public class GameService {
    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private MoveRepository moveRepository;
    @Autowired
    private UserService userService;

    public Optional<Game> findById(Long id) {
        return gameRepository.findById(id);
    }

    @Transactional
    public Game createGame(Long whiteId, Long blackId) {
        User white = userService.findById(whiteId)
                .orElseThrow(() -> new IllegalArgumentException("White player not found"));
        User black = userService.findById(blackId)
                .orElseThrow(() -> new IllegalArgumentException("Black player not found"));

        Game game = new Game();
        game.setPlayerWhite(white);
        game.setPlayerBlack(black);
        game.setFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
        game.setStatus("ongoing");
        game = gameRepository.save(game);

        userService.setPlayingStatus(white, game.getId());
        userService.setPlayingStatus(black, game.getId());

        return game;
    }

    @Transactional
    public void saveMove(Game game, String moveNotation, String fen) {
        Move move = new Move();
        move.setGame(game);
        move.setMoveNotation(moveNotation);
        move.setFen(fen);
        move.setMoveTime(LocalDateTime.now());
        moveRepository.save(move);

        game.setFen(fen);
        gameRepository.save(game);
    }

    @Transactional
    public void finishGame(Game game, User winner, String finishReason) {
        game.setStatus("finished");
        game.setWinner(winner);
        game.setFinishReason(finishReason);
        game.setEndTime(LocalDateTime.now());
        gameRepository.save(game);

        userService.resetStatus(game.getPlayerWhite());
        userService.resetStatus(game.getPlayerBlack());
    }

    @Transactional
    public void finishGameById(Long gameId, Long winnerId, String finishReason) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found"));
        User winner = userService.findById(winnerId)
                .orElseThrow(() -> new RuntimeException("Winner not found"));
        finishGame(game, winner, finishReason);
    }

}
