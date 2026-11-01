package com.streaming.feed.dto;

import java.util.UUID;

public record RecommendedChannelResponse(
        UUID channelId,
        String slug,
        String displayName,
        String avatarUrl,
        long liveStreamCount
) {}