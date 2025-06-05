package com.zanra.catur.kafka.producers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.zanra.catur.events.DisconnectUserFromGameEvent;
import com.zanra.catur.events.GameCreatedEvent;
import com.zanra.catur.events.GameFinishedEvent;
import com.zanra.catur.events.MatchMakingRequestEvent;
import com.zanra.catur.events.MoveMadeEvent;
import com.zanra.catur.events.ReconnectUserToGameEvent;
import com.zanra.catur.events.SurenderRequestEvent;

@Service
public class GameEventProducer {
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void sendGameCreated(GameCreatedEvent event) {
        kafkaTemplate.send("game-events", event.getEventId(), event);
    }

    public void sendMoveMade(MoveMadeEvent event) {
        kafkaTemplate.send("game-events", event.getEventId(), event);
    }

    public void sendGameFinished(GameFinishedEvent event) {
        kafkaTemplate.send("game-events", event.getEventId(), event);
    }

    public void sendSurenderRequest(SurenderRequestEvent event) {
        kafkaTemplate.send("game-events", event.getEventId(), event);
    }

    public void sendMatchMakingRequest(MatchMakingRequestEvent event) {
        kafkaTemplate.send("matchmaking-events", event.getEventId(), event);
    }

    public void sendDisconnectUserFromGameRequest(DisconnectUserFromGameEvent event) {
        kafkaTemplate.send("game-events", event.getEventId(), event);
    }

    public void sendReconnectUserToGameRequest(ReconnectUserToGameEvent event) {
        kafkaTemplate.send("game-events", event.getEventId(), event);
    }

}
