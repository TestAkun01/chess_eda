package com.zanra.catur.models;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "match_queue")
public class MatchQueue {

    @Id
    @GeneratedValue
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String status; // "waiting", "matched", etc.

    private LocalDateTime createdAt;

    public User getUser() {
        return user;
    }

    public String getStatus() {
        return status;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setUser(User currentUser) {
        user = currentUser;
    }

    public void setStatus(String waiting) {
        status = waiting;
    }

    public void setCreatedAt(LocalDateTime now) {
        createdAt = now;
    }
}
