package com.zanra.catur.kafka.consumers;

import com.zanra.catur.dto.ResponseDTO;
import com.zanra.catur.events.ChatMessageSentEvent;
import com.zanra.catur.models.ChatMessage;
import com.zanra.catur.services.ChatMessageService;
import com.zanra.catur.utils.IdempotencyUtil;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class ChatEventConsumer {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private IdempotencyUtil idempotencyUtil;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @KafkaListener(topics = "chat-messages", groupId = "chat-group", containerFactory = "kafkaListenerContainerFactory")
    public void consumeChatMessageEvent(ConsumerRecord<String, Object> record) {
        Object event = record.value();
        if (event instanceof ChatMessageSentEvent) {
            processChatMessageSentEvent((ChatMessageSentEvent) event);
        }

    }

    private void processChatMessageSentEvent(ChatMessageSentEvent event) {
        if (idempotencyUtil.isEventProcessed(event.getEventId())) {
            return;
        }
        ChatMessage savedMessage = chatMessageService.saveMessage(event.getSender(), event.getMessage());
        messagingTemplate.convertAndSend("/topic/messages", ResponseDTO.success("Message Sended", savedMessage));
        idempotencyUtil.markEventAsProcessed(event.getEventId());
    }
}
