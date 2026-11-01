package com.streaming.feed.dto;

import java.time.Instant;
import java.util.UUID;

public record StreamResponse(
        UUID id,
        UUID channelId,
        String title,
        String description,
        String category,
        String thumbnailUrl,
        String status,
        Instant scheduledAt,
        Instant startedAt,
        Instant endedAt,
        String playbackUrl
) {
}