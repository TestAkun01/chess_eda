package com.zanra.catur.repositories;


import org.springframework.data.jpa.repository.JpaRepository;

import com.zanra.catur.models.Game;

public interface GameRepository extends JpaRepository<Game, Long> {}
