package com.streaming.streamservice.dto.client;

import java.util.UUID;

public record InternalChannelResponse(
        UUID id,
        UUID ownerUserId,
        String slug,
        String displayName,
        String status
) {}