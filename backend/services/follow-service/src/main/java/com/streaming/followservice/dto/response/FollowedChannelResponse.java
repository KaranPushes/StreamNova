package com.streaming.followservice.dto.response;

import java.time.Instant;
import java.util.UUID;

public record FollowedChannelResponse(
        UUID channelId,
        Instant followedAt
) {
}