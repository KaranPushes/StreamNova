package com.streaming.userservice.dto.response;

import lombok.Builder;

import java.time.Instant;

@Builder
public record PublicUserResponse(

        String username,
        String displayName,
        String bio,
        String avatarUrl,
        String bannerUrl,
        String websiteUrl,
        String location,
        Instant createdAt

) {
}