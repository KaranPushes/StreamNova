package com.streaming.feed.dto;

import java.time.Instant;
import java.util.UUID;

public record LiveStreamResponse(
        UUID streamId,
        UUID channelId,

        String channelSlug,
        String channelDisplayName,
        String channelDescription,
        String channelAvatarUrl,
        String channelBannerUrl,

        String title,
        String description,
        String category,
        String thumbnailUrl,
        Instant startedAt,
        String playbackUrl
) {
}