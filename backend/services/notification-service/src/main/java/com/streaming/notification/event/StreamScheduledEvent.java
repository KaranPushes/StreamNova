package com.streaming.notification.event;

import java.time.Instant;
import java.util.UUID;

public record StreamScheduledEvent(
        UUID streamId,
        UUID channelId,
        String streamTitle,
        Instant scheduledAt
) {
}