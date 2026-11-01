package com.streaming.feed.dto;

import java.time.Instant;
import java.util.UUID;

public record ChannelResponse(
        UUID id,
        UUID ownerUserId,
        String slug,
        String displayName,
        String description,
        String avatarUrl,
        String bannerUrl,
        String status,
        Instant createdAt,
        Instant updatedAt
) {
}