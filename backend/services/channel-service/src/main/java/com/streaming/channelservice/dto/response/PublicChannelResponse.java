package com.streaming.channelservice.dto.response;

import java.time.Instant;
import java.util.UUID;

public record PublicChannelResponse(

        UUID id,
        String slug,
        String displayName,
        String description,
        String avatarUrl,
        String bannerUrl,
        Instant createdAt

) {
}