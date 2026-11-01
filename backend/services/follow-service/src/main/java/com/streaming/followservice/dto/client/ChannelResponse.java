package com.streaming.followservice.dto.client;

import java.util.UUID;

public record ChannelResponse(
        UUID id,
        UUID ownerUserId,
        String slug,
        String displayName,
        String status
) {
}