package com.zanra.catur.kafka.producers;

import com.zanra.catur.events.ChatMessageSentEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ChatEventProducer {
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void sendChatMessageEvent(ChatMessageSentEvent event) {
        kafkaTemplate.send("chat-messages", event.getEventId(), event);
    }
}
