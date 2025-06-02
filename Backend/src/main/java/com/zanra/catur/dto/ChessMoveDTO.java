package com.zanra.catur.dto;

public class ChessMoveDTO {
    private Long matchId;
    private String from;        // Contoh: "e2"
    private String to;          // Contoh: "e4"
    private String fen;         // Posisi FEN setelah langkah
    private String moveNotation; // SAN notation, contoh: "e4"
    private String movedBy;     // userId atau username pemain yang melakukan move

    // Getter & Setter
    public Long getMatchId() {
        return matchId;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getFen() {
        return fen;
    }

    public String getMoveNotation() {
        return moveNotation;
    }

    public String getMovedBy() {
        return movedBy;
    }
}
