package com.streaming.channelservice.dto.client;

import com.streaming.channelservice.entity.ChannelStatus;

import java.util.UUID;

public record InternalChannelResponse(
        UUID id,
        UUID ownerUserId,
        String slug,
        String displayName,
        ChannelStatus status
) {}