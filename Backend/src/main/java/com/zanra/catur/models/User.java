package com.zanra.catur.models;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User implements Serializable {
    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    private String passwordHash;

    private int rating;

    @Column(nullable = true)
    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @Column(nullable = true)
    private Long gameId;

    public void resetStatus() {
        this.gameId = null;
        this.status = UserStatus.FREE;
    }

    public enum UserStatus {
        FREE,
        PLAYING,
        IN_GAME,
        DISCONNECTED,
        MATCHING
    }

}
