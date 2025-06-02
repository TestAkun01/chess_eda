package com.zanra.catur.kafka.consumers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class ChessMoveConsumer {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @KafkaListener(topics = "chess-moves", groupId = "chess-group")
    public void listen(String message) {
        // Contoh payload: "gameId|FEN_STRING"
        String[] parts = message.split("\\|");
        if (parts.length != 2) {
            System.err.println("Invalid message format: " + message);
            return;
        }

        String gameId = parts[0];
        String fen = parts[1];

        // Cetak ke log atau update ke database
        System.out.println("Game: " + gameId + " -> New FEN: " + fen);

        // Kirim update ke frontend via WebSocket
        messagingTemplate.convertAndSend("/topic/game/" + gameId, fen);
    }
}
