package com.streaming.streamservice.dto.response;

public record CategoryResponse(
        String name,
        long liveStreamCount
) {
}