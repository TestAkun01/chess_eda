package com.zanra.catur.kafka.producers;

import com.zanra.catur.events.UserRegisteredEvent;
import com.zanra.catur.events.UserLoggedInEvent;
import com.zanra.catur.events.UserLoggedOutEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class AuthEventProducer {
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void sendUserRegisteredEvent(UserRegisteredEvent event) {
        kafkaTemplate.send("auth-events", event.getEventId(), event);
    }

    public void sendUserLoggedInEvent(UserLoggedInEvent event) {
        kafkaTemplate.send("auth-events", event.getEventId(), event);
    }

    public void sendUserLoggedOutEvent(UserLoggedOutEvent event) {
        kafkaTemplate.send("auth-events", event.getEventId(), event);
    }
}