package com.zanra.catur.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    private String passwordHash;

    private int rating;
    @Column(nullable=true)
    private String status;
    @Column(nullable=true)
    private Long gameId;
    // getter setter

    public void setStatus(String status) {
        this.status = status;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    } 

    public void resetStatus() {
        this.gameId = null;
        this.status = "free";
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public int getRating() {
        return rating;
    }

    public String getStatus() {
        return status;
    }

    public Long getGameId() {
        return gameId;
    }


}
