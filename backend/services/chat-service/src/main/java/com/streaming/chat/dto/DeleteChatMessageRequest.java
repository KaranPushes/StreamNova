package com.streaming.chat.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record DeleteChatMessageRequest(
        @NotNull UUID messageId
) {
}