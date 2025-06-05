package com.zanra.catur.kafka.consumers;

import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.zanra.catur.events.GameCreatedEvent;
import com.zanra.catur.events.MatchMakingRequestEvent;
import com.zanra.catur.kafka.producers.GameEventProducer;
import com.zanra.catur.models.User;
import com.zanra.catur.services.UserService;
import com.zanra.catur.utils.IdempotencyUtil;

@Component
public class MatchMakingConsumer {
    @Autowired
    private UserService userService;
    @Autowired
    private GameEventProducer gameEventProducer;
    @Autowired
    private IdempotencyUtil idempotencyUtil;
    private final Queue<Long> matchmakingQueue = new ConcurrentLinkedQueue<>();

    @KafkaListener(topics = "matchmaking-events", groupId = "matchmaking-group")
    public void consumeMatchMakingEvents(ConsumerRecord<String, Object> record) {
        Object event = record.value();

        if (event instanceof MatchMakingRequestEvent) {
            processMatchMakingRequestEvent((MatchMakingRequestEvent) event);
        }
    }

    private void processMatchMakingRequestEvent(MatchMakingRequestEvent event) {
        if (idempotencyUtil.isEventProcessed(event.getEventId()))
            return;

        matchmakingQueue.add(event.getUserId());
        Optional<User> userOpt = userService.findById(event.getUserId());
        if (userOpt.isEmpty()) {
            matchmakingQueue.remove(event.getUserId());
            return;
        }

        userService.updateStatus(userOpt.get(), User.UserStatus.MATCHING);

        if (matchmakingQueue.size() >= 2) {
            Long whiteId = matchmakingQueue.poll();
            Long blackId = matchmakingQueue.poll();

            GameCreatedEvent createdEvent = new GameCreatedEvent();
            createdEvent.setEventId(UUID.randomUUID().toString());
            createdEvent.setWhitePlayerId(whiteId);
            createdEvent.setBlackPlayerId(blackId);

            gameEventProducer.sendGameCreated(createdEvent);
        }

        idempotencyUtil.markEventAsProcessed(event.getEventId());
    }
}
