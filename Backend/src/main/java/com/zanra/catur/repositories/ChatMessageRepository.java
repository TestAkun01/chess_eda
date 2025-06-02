package com.zanra.catur.repositories;


import org.springframework.data.jpa.repository.JpaRepository;

import com.zanra.catur.models.ChatMessage;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {}

