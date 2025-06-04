package com.zanra.catur.services;

import com.zanra.catur.models.ChatMessage;
import com.zanra.catur.models.User;
import com.zanra.catur.repositories.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ChatMessageService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    public ChatMessage saveMessage(User sender, String message) {
        if (sender == null) {
            throw new IllegalArgumentException("Sender cannot be null");
        }
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Message cannot be empty");
        }

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSender(sender);
        chatMessage.setMessage(message);
        chatMessage.setSentAt(LocalDateTime.now());

        return chatMessageRepository.save(chatMessage);
    }

    public List<ChatMessage> getAllMessages() {
        return chatMessageRepository.findAllByOrderBySentAtAsc();
    }

    public List<ChatMessage> getMessagesBySender(User sender) {
        if (sender == null) {
            throw new IllegalArgumentException("Sender cannot be null");
        }
        return chatMessageRepository.findBySender(sender);
    }

    public Optional<ChatMessage> getMessageById(Long messageId) {
        return chatMessageRepository.findById(messageId);
    }

    public void deleteMessage(Long messageId) {
        if (!chatMessageRepository.existsById(messageId)) {
            throw new IllegalArgumentException("Message not found");
        }
        chatMessageRepository.deleteById(messageId);
    }
}