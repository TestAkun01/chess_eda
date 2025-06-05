package com.zanra.catur.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.zanra.catur.models.Game;

public interface GameRepository extends JpaRepository<Game, Long> {
    @Query("SELECT g FROM Game g WHERE (g.playerWhite.id = :userId OR g.playerBlack.id = :userId) AND g.status = :status")
    Optional<Game> findByPlayerWhiteIdOrPlayerBlackIdAndStatus(
            @Param("userId") Long userId1,
            @Param("userId") Long userId2,
            @Param("status") Game.GameStatus status);

    @Query("SELECT g FROM Game g WHERE g.id = :gameId AND g.status = 'ONGOING'")
    Optional<Game> findActiveGameById(@Param("gameId") Long gameId);
}
