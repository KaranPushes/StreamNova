package com.streaming.chat.dto;

import java.time.Instant;
import java.util.UUID;

public record ChatMessageResponse(
        UUID id,
        UUID streamId,
        String userId,
        String username,
        String message,
        Instant createdAt
) {
}