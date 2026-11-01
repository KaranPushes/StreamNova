package com.streaming.mediaservice.dto.response;

import com.streaming.mediaservice.entity.MediaType;

import java.time.Instant;
import java.util.UUID;

public record MediaResponse(
        UUID id,
        MediaType mediaType,
        String originalFileName,
        String url,
        String contentType,
        long fileSize,
        Instant createdAt
) {
}