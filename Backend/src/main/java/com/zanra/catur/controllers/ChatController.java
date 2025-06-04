package com.zanra.catur.controllers;

import com.zanra.catur.kafka.producers.ChatEventProducer;
import com.zanra.catur.models.ChatMessage;
import com.zanra.catur.models.User;
import com.zanra.catur.services.ChatMessageService;
import com.zanra.catur.dto.ChatDTO;
import com.zanra.catur.dto.ResponseDTO;
import com.zanra.catur.events.ChatMessageSentEvent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    @Autowired
    private ChatEventProducer chatEventProducer;

    @Autowired
    private ChatMessageService chatMessageService;

    @GetMapping("/messages")
    public ResponseEntity<?> getAllMessages(HttpServletRequest request) {
        User user = (User) request.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ResponseDTO.error(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication required"));
        }

        List<ChatMessage> messages = chatMessageService.getAllMessages();
        return ResponseEntity.ok(new ChatDTO.Response(messages));
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@RequestBody ChatDTO.Request requestDTO,
            HttpServletRequest request) {
        User user = (User) request.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ResponseDTO.error(HttpStatus.UNAUTHORIZED, "Unauthorized"));
        }

        if (requestDTO.getMessage() == null || requestDTO.getMessage().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ResponseDTO.error(HttpStatus.BAD_REQUEST, "Message cannot be empty"));
        }

        ChatMessageSentEvent event = new ChatMessageSentEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setSender(user);
        event.setMessage(requestDTO.getMessage());

        chatEventProducer.sendChatMessageEvent(event);
        return ResponseEntity.ok(ResponseDTO.success("Message sending initiated"));
    }
}
