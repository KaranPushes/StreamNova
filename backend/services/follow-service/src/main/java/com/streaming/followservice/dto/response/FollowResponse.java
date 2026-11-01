package com.streaming.followservice.dto.response;

import java.time.Instant;
import java.util.UUID;

public record FollowResponse(
        UUID id,
        UUID channelId,
        Instant followedAt
) {
}