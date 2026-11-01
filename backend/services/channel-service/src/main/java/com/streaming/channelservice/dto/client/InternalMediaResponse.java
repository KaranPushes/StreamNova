package com.streaming.channelservice.dto.client;

import com.streaming.channelservice.entity.MediaType;

import java.util.UUID;

public record InternalMediaResponse(
        UUID id,
        UUID ownerUserId,
        MediaType mediaType,
        String url
) {
}