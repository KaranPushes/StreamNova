package com.streaming.authservice.dto.session;

import lombok.Builder;

@Builder
public record SessionInfo(

        String deviceName,

        String browser,

        String operatingSystem,

        String userAgent,

        String ipAddress
) {
}