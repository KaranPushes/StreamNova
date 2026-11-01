package com.streaming.feed.dto;

import java.util.UUID;

public record ChannelSearchResponse(
        UUID id,
        String slug,
        String displayName,
        String avatarUrl
) {
}