package com.streaming.channelservice.dto.response;

public record SlugAvailabilityResponse(
        String slug,
        boolean available
) {
}