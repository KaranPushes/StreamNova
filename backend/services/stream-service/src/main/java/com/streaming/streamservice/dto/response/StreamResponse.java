package com.streaming.streamservice.dto.response;

import com.streaming.streamservice.entity.StreamStatus;

import java.time.Instant;
import java.util.UUID;

public record StreamResponse(
        UUID id,
        UUID channelId,
        String title,
        String description,
        String category,
        String thumbnailUrl,
        StreamStatus status,
        Instant scheduledAt,
        Instant startedAt,
        Instant endedAt,
        Instant createdAt,
        Instant updatedAt,
        String playbackUrl
) {}