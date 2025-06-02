package com.zanra.catur.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zanra.catur.models.MatchQueue;

public interface MatchQueueRepository extends JpaRepository<MatchQueue, Long> {
    Optional<MatchQueue> findFirstByStatusOrderByCreatedAtAsc(String status);   
}
