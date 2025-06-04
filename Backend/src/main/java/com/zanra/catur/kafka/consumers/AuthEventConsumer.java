package com.zanra.catur.kafka.consumers;

import com.zanra.catur.events.UserRegisteredEvent;
import com.zanra.catur.dto.AuthDTO;
import com.zanra.catur.dto.ResponseDTO;
import com.zanra.catur.events.UserLoggedInEvent;
import com.zanra.catur.events.UserLoggedOutEvent;
import com.zanra.catur.models.User;
import com.zanra.catur.services.UserService;
import com.zanra.catur.utils.IdempotencyUtil;
import com.zanra.catur.utils.JwtUtil;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuthEventConsumer {
    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private IdempotencyUtil idempotencyUtil;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @KafkaListener(topics = "auth-events", groupId = "auth-group", containerFactory = "kafkaListenerContainerFactory")
    public void consumeAuthEvent(ConsumerRecord<String, Object> record) {
        Object event = record.value();

        if (event instanceof UserRegisteredEvent) {
            processUserRegisteredEvent((UserRegisteredEvent) event);
        } else if (event instanceof UserLoggedInEvent) {
            processUserLoggedInEvent((UserLoggedInEvent) event);
        } else if (event instanceof UserLoggedOutEvent) {
            processUserLoggedOutEvent((UserLoggedOutEvent) event);
        }
    }

    private void processUserRegisteredEvent(UserRegisteredEvent event) {
        if (idempotencyUtil.isEventProcessed(event.getEventId())) {
            return;
        }

        if (userService.usernameExists(event.getUsername())) {
            messagingTemplate.convertAndSend(
                    "/topic/auth/register/" + event.getEventId(),
                    ResponseDTO.error(
                            HttpStatus.BAD_REQUEST,
                            "Username already taken"));
            return;
        }

        userService.register(event.getUsername(), event.getPassword());
        idempotencyUtil.markEventAsProcessed(event.getEventId());
        messagingTemplate.convertAndSend(
                "/topic/auth/register/" + event.getEventId(),
                ResponseDTO.success("User registered successfully"));
    }

    private void processUserLoggedInEvent(UserLoggedInEvent event) {
        if (idempotencyUtil.isEventProcessed(event.getEventId())) {
            return;
        }

        Optional<User> userOpt = userService.authenticate(event.getUsername(), event.getPassword());
        if (userOpt.isEmpty()) {
            messagingTemplate.convertAndSend(
                    "/topic/auth/login/" + event.getEventId(),
                    ResponseDTO.error(
                            HttpStatus.UNAUTHORIZED,
                            "Invalid credentials"));
            return;
        }

        User user = userOpt.get();
        String token = jwtUtil.generateToken(user.getId());

        messagingTemplate.convertAndSend(
                "/topic/auth/login/" + event.getEventId(),
                new AuthDTO.Response(user.getId(), user.getUsername(), token));
        idempotencyUtil.markEventAsProcessed(event.getEventId());
    }

    private void processUserLoggedOutEvent(UserLoggedOutEvent event) {
        if (idempotencyUtil.isEventProcessed(event.getEventId())) {
            return;
        }

        idempotencyUtil.markEventAsProcessed(event.getEventId());
        messagingTemplate.convertAndSend(
                "/topic/auth/logout/" + event.getEventId(),
                ResponseDTO.success("Logged out successfully"));
    }
}
