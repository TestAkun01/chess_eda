package com.zanra.catur.services;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zanra.catur.models.Game;
import com.zanra.catur.models.Move;
import com.zanra.catur.models.User;
import com.zanra.catur.repositories.GameRepository;
import com.zanra.catur.repositories.MoveRepository;
import com.zanra.catur.repositories.UserRepository;

@Service
public class GameService {
    @Autowired private GameRepository gameRepository;
    @Autowired private MoveRepository moveRepository;
    @Autowired private UserRepository userRepository;

    public Game createGame(User white, User black) {
        Game game = new Game();
        game.setPlayerWhite(white);
        game.setPlayerBlack(black);
        game.setFen("startpos");
        game.setStatus("ongoing");
        return gameRepository.save(game);
    }

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

    public void updateStatusUser(Long idGame, User user) {
        user.setGameId(idGame);
        user.setStatus("playing");
        userRepository.save(user);
    }

    public void resetStatusUser(User user) {
        user.resetStatus();
        userRepository.save(user);
    } 
}
