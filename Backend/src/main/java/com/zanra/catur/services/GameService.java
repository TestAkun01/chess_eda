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
import com.zanra.catur.repositories.UserRepository;

@Service
public class GameService {
    @Autowired
    private UserRepository userRepository;
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
        game.setStatus(Game.GameStatus.ONGOING);
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
    public void finishGame(Game game, User winner, Game.FinishReason finishReason) {
        game.setStatus(Game.GameStatus.FINISHED);
        game.setWinner(winner);
        game.setFinishReason(finishReason);
        game.setEndTime(LocalDateTime.now());
        gameRepository.save(game);

        userService.resetStatus(game.getPlayerWhite());
        userService.resetStatus(game.getPlayerBlack());
    }

    @Transactional
    public void finishGameById(Long gameId, Long winnerId, Game.FinishReason finishReason) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found"));
        User winner = userService.findById(winnerId)
                .orElseThrow(() -> new RuntimeException("Winner not found"));
        finishGame(game, winner, finishReason);
    }

    public Optional<Game> findActiveGameByUserId(Long userId) {
        return gameRepository.findByPlayerWhiteIdOrPlayerBlackIdAndStatus(
                userId, userId, Game.GameStatus.ONGOING);
    }

    public Game resumeGame(Long userId) {
        Optional<Game> activeGame = findActiveGameByUserId(userId);
        if (activeGame.isPresent()) {
            Game game = activeGame.get();
            // Update user status ke dalam game
            User user = userRepository.findById(userId).orElseThrow();
            user.setStatus(User.UserStatus.IN_GAME);
            user.setGameId(game.getId());
            userRepository.save(user);
            return game;
        }
        return null;
    }

    // Method untuk disconnect user dari game (tidak finish game)
    public void disconnectUserFromGame(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        if (user.getGameId() != null) {
            user.setStatus(User.UserStatus.DISCONNECTED);
            userRepository.save(user);
        }
    }

    // Method untuk reconnect user ke game
    public Game reconnectUserToGame(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        if (user.getGameId() != null) {
            Optional<Game> game = gameRepository.findById(user.getGameId());
            if (game.isPresent() && Game.GameStatus.ONGOING.equals(game.get().getStatus())) {
                user.setStatus(User.UserStatus.IN_GAME);
                userRepository.save(user);
                return game.get();
            }
        }
        return null;
    }

}
