package com.streaming.mediaservice.dto.response;

import com.streaming.mediaservice.entity.MediaType;

import java.util.UUID;

public record InternalMediaResponse(
        UUID id,
        UUID ownerUserId,
        MediaType mediaType,
        String url
) {
}