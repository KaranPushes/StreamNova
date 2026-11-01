package com.streaming.notification.event;

import java.util.UUID;

public record SecurityNotificationEvent(
        UUID userId,
        String title,
        String message
) {
}