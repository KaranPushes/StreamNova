package com.streaming.userservice.dto.client;

import java.util.UUID;

public record InternalMediaResponse(
        UUID id,
        UUID ownerUserId,
        String mediaType,
        String url
) {
}