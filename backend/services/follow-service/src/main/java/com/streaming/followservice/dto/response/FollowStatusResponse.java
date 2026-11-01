package com.streaming.followservice.dto.response;

import java.util.UUID;

public record FollowStatusResponse(
        UUID channelId,
        boolean following
) {
}