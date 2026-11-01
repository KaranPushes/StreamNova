package com.streaming.streamservice.dto.client;

import com.streaming.streamservice.entity.MediaType;

import java.util.UUID;

public record InternalMediaResponse(
        UUID id,
        UUID ownerUserId,
        MediaType mediaType,
        String url
) {}