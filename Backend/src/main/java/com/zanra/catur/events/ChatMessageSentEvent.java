package com.zanra.catur.events;

import com.zanra.catur.models.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageSentEvent extends BaseEvent {
    private User sender;
    private String message;
}
