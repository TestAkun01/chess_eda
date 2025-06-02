package com.zanra.catur.repositories;


import org.springframework.data.jpa.repository.JpaRepository;

import com.zanra.catur.models.Move;


public interface MoveRepository extends JpaRepository<Move, Long> {}

