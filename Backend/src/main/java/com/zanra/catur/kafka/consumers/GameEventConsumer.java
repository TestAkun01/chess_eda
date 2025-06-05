package com.zanra.catur.kafka.consumers;

import java.util.Map;
import java.util.UUID;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import com.zanra.catur.events.DisconnectUserFromGameEvent;
import com.zanra.catur.events.GameCreatedEvent;
import com.zanra.catur.events.GameFinishedEvent;
import com.zanra.catur.events.MoveMadeEvent;
import com.zanra.catur.events.ReconnectUserToGameEvent;
import com.zanra.catur.events.SurenderRequestEvent;
import com.zanra.catur.kafka.producers.GameEventProducer;
import com.zanra.catur.models.Game;
import com.zanra.catur.services.GameService;
import com.zanra.catur.utils.IdempotencyUtil;

@Component
public class GameEventConsumer {

    @Autowired
    private GameEventProducer gameEventProducer;
    @Autowired
    private GameService gameService;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private IdempotencyUtil idempotencyUtil;

    @KafkaListener(topics = "game-events", groupId = "game-group")
    public void consumeGameEvents(ConsumerRecord<String, Object> record) {
        Object event = record.value();

        if (event instanceof GameCreatedEvent) {
            processGameCreatedEvent((GameCreatedEvent) event);
        } else if (event instanceof GameFinishedEvent) {
            processGameFinishedEvent((GameFinishedEvent) event);
        } else if (event instanceof MoveMadeEvent) {
            processMoveMadeEvent((MoveMadeEvent) event);
        } else if (event instanceof SurenderRequestEvent) {
            processSurenderRequestEvent((SurenderRequestEvent) event);
        } else if (event instanceof DisconnectUserFromGameEvent) {
            processDisconnectUserFromGameEvent((DisconnectUserFromGameEvent) event);
        } else if (event instanceof ReconnectUserToGameEvent) {
            processReconnectUserToGameEvent((ReconnectUserToGameEvent) event);
        }
    }

    private void processGameCreatedEvent(GameCreatedEvent event) {
        if (idempotencyUtil.isEventProcessed(event.getEventId())) {
            return;
        }
        Game game = gameService.createGame(event.getWhitePlayerId(), event.getBlackPlayerId());
        messagingTemplate.convertAndSend("/topic/matchmaking/" + event.getWhitePlayerId(), game);
        messagingTemplate.convertAndSend("/topic/matchmaking/" + event.getBlackPlayerId(), game);
        idempotencyUtil.markEventAsProcessed(event.getEventId());
    }

    private void processGameFinishedEvent(GameFinishedEvent event) {
        if (idempotencyUtil.isEventProcessed(event.getEventId())) {
            return;
        }
        gameService.finishGameById(event.getGameId(), event.getWinnerId(), event.getReason());
        messagingTemplate.convertAndSend("/topic/game/" + event.getGameId() + "/finish", event);
        idempotencyUtil.markEventAsProcessed(event.getEventId());
    }

    private void processMoveMadeEvent(MoveMadeEvent event) {
        if (idempotencyUtil.isEventProcessed(event.getEventId())) {
            return;
        }
        Piece promotion = Piece.NONE;
        if (event.getPromotion() != null) {
            promotion = Piece.fromFenSymbol(event.getPromotion());
        }
        Game game = gameService.findById(event.getGameId()).orElseThrow();
        Board board = new Board();
        board.loadFromFen(game.getFen());
        Move move = new Move(Square.valueOf(event.getFrom().toUpperCase()),
                Square.valueOf(event.getTo().toUpperCase()), promotion);
        board.doMove(move);

        gameService.saveMove(game, event.getMove(), board.getFen());

        if (board.isMated()) {
            GameFinishedEvent finishEvent = new GameFinishedEvent();
            finishEvent.setEventId(UUID.randomUUID().toString());
            finishEvent.setGameId(event.getGameId());
            finishEvent.setWinnerId(event.getPlayerId());
            finishEvent.setReason(Game.FinishReason.CHECKMATE);
            gameEventProducer.sendGameFinished(finishEvent);
        } else if (board.isDraw()) {
            GameFinishedEvent finishEvent = new GameFinishedEvent();
            finishEvent.setEventId(UUID.randomUUID().toString());
            finishEvent.setGameId(event.getGameId());
            finishEvent.setReason(Game.FinishReason.DRAW_AGREEMENT);
            gameEventProducer.sendGameFinished(finishEvent);
        }

        messagingTemplate.convertAndSend("/topic/game/" + event.getGameId() + "/move",
                Map.of("fen", board.getFen()));
        idempotencyUtil.markEventAsProcessed(event.getEventId());
    }

    private void processSurenderRequestEvent(SurenderRequestEvent event) {
        if (idempotencyUtil.isEventProcessed(event.getEventId())) {
            return;
        }
        Game game = gameService.findById(event.getGameId()).orElseThrow();
        if (!game.getPlayerWhite().getId().equals(event.getPlayerId())
                && !game.getPlayerBlack().getId().equals(event.getPlayerId())) {
            throw new IllegalArgumentException("Player not in game");
        }

        Long winnerId = game.getPlayerWhite().getId().equals(event.getPlayerId())
                ? game.getPlayerBlack().getId()
                : game.getPlayerWhite().getId();
        GameFinishedEvent gameEvent = new GameFinishedEvent();
        gameEvent.setEventId(UUID.randomUUID().toString());
        gameEvent.setGameId(event.getGameId());
        gameEvent.setWinnerId(winnerId);
        gameEvent.setReason(Game.FinishReason.SURRENDER);
        gameEventProducer.sendGameFinished(gameEvent);
        idempotencyUtil.markEventAsProcessed(event.getEventId());
    }

    private void processDisconnectUserFromGameEvent(DisconnectUserFromGameEvent event) {
        if (idempotencyUtil.isEventProcessed(event.getEventId())) {
            return;
        }
        Game game = gameService.findById(event.getGameId()).orElseThrow();
        Long opponentId = game.getPlayerWhite().getId().equals(event.getUserId())
                ? game.getPlayerBlack().getId()
                : game.getPlayerWhite().getId();
        gameService.disconnectUserFromGame(event.getUserId());
        messagingTemplate.convertAndSend("/topic/game/" + game.getId() + "/disconnect/" + opponentId,
                Map.of("opponentId", opponentId));
        idempotencyUtil.markEventAsProcessed(event.getEventId());
    }

    private void processReconnectUserToGameEvent(ReconnectUserToGameEvent event) {
        if (idempotencyUtil.isEventProcessed(event.getEventId())) {
            return;
        }
        Game game = gameService.findById(event.getGameId()).orElseThrow();
        Long opponentId = game.getPlayerWhite().getId().equals(event.getUserId())
                ? game.getPlayerBlack().getId()
                : game.getPlayerWhite().getId();
        messagingTemplate.convertAndSend("/topic/game/" + game.getId() + "/reconnect/" + opponentId,
                Map.of("opponentId", opponentId));
        idempotencyUtil.markEventAsProcessed(event.getEventId());
    }
}