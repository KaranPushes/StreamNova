package com.streaming.channelservice.dto.response;

import com.streaming.channelservice.entity.ChannelStatus;

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
        ChannelStatus status,
        Instant createdAt,
        Instant updatedAt

) {
}