package com.streaming.userservice.dto.response;

import com.streaming.userservice.entity.UserStatus;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record UserResponse(

        UUID id,
        String username,
        String displayName,
        String email,

        String bio,

        UUID avatarMediaId,
        String avatarUrl,

        UUID bannerMediaId,
        String bannerUrl,

        String websiteUrl,
        String location,

        UserStatus status,
        Instant createdAt,
        Instant updatedAt

) {
}