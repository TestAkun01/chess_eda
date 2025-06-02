package com.zanra.catur.kafka.producers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ChessMoveProducer {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private final String topic = "chess-moves";

    public void sendMove(Long gameId, String fen) {
        String payload = gameId + "|" + fen;
        kafkaTemplate.send(topic, payload);
    }
}

