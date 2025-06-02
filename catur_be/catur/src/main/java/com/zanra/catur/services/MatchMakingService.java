package com.zanra.catur.services;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zanra.catur.models.Game;
import com.zanra.catur.models.User;
import com.zanra.catur.repositories.MatchQueueRepository;
import com.zanra.catur.repositories.GameRepository;
import com.zanra.catur.models.*;

@Service
public class MatchMakingService {

    @Autowired private MatchQueueRepository queueRepo;
    @Autowired private GameRepository gameRepo;

    public Game tryFindMatch(User currentUser) {
        // Cek apakah ada user lain yang waiting
        Optional<MatchQueue> waiting = queueRepo
            .findFirstByStatusOrderByCreatedAtAsc("waiting");

        if (waiting.isPresent() && !waiting.get().getUser().getId().equals(currentUser.getId())) {
            // Match ditemukan
            User opponent = waiting.get().getUser();
            queueRepo.delete(waiting.get());

            Game game = new Game();
            game.setPlayerWhite(opponent);
            game.setPlayerBlack(currentUser);
            game.setFen("startpos");
            game.setStatus("ongoing");
            game.setStartTime(LocalDateTime.now());

            return gameRepo.save(game);
        } else {
            // Tidak ada match, user masuk waiting list
            MatchQueue queue = new MatchQueue();
            queue.setUser(currentUser);
            queue.setStatus("waiting");
            queue.setCreatedAt(LocalDateTime.now());
            queueRepo.save(queue);
            return null;
        }
    }
}
