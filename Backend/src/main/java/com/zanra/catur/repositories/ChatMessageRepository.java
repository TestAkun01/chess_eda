package com.zanra.catur.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zanra.catur.models.ChatMessage;
import com.zanra.catur.models.User;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findBySender(User sender);

    List<ChatMessage> findAllByOrderBySentAtDesc();

    List<ChatMessage> findAllByOrderBySentAtAsc();
}
