package com.streaming.notification.dto;

import com.streaming.notification.entity.NotificationType;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        NotificationType type,
        String title,
        String message,
        UUID referenceId,
        boolean read,
        Instant createdAt
) {
}