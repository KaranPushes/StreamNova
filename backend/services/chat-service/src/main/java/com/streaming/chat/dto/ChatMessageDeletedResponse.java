package com.streaming.chat.dto;

import java.time.Instant;
import java.util.UUID;

public record ChatMessageDeletedResponse(
        UUID messageId,
        UUID streamId,
        UUID userId,
        Instant deletedAt
) {
}