package com.streaming.followservice.dto.response;

import java.util.UUID;

public record FollowerCountResponse(
        UUID channelId,
        long followerCount
) {
}