package com.zanra.catur.utils;

import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class IdempotencyUtil {
    private final Set<String> processedEventIds = new HashSet<>();

    public boolean isEventProcessed(String eventId) {
        return processedEventIds.contains(eventId);
    }

    public void markEventAsProcessed(String eventId) {
        processedEventIds.add(eventId);
    }
}