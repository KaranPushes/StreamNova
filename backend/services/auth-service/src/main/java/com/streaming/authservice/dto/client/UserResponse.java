package com.streaming.authservice.dto.client;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(

        UUID id,

        String username,

        String displayName,

        String email,

        String status,

        Instant createdAt,

        Instant updatedAt
) {
}