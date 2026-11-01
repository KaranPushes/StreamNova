package com.streaming.feed.dto;

public record CategoryResponse(
        String name,
        long liveStreamCount
) {
}