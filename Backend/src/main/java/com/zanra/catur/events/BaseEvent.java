package com.zanra.catur.events;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class BaseEvent {
    @JsonProperty("eventId")
    private String eventId = UUID.randomUUID().toString();

    @JsonProperty("createdAt")
    private Instant createdAt = Instant.now();
}