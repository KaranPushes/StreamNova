package com.streaming.authservice.dto.response;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record SessionSummary(

        UUID id,

        String deviceName,

        String browser,

        String operatingSystem,

        String ipAddress,

        Instant lastAccessedAt,

        Instant createdAt,

        boolean current
) {
}