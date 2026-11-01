package com.streaming.authservice.dto.internal;

import lombok.Builder;

@Builder
public record SessionMetadata(

        String deviceName,

        String ipAddress

) {
}