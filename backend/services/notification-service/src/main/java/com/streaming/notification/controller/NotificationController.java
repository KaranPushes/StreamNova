package com.streaming.notification.controller;

import com.streaming.notification.dto.NotificationResponse;
import com.streaming.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> getNotifications(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "false") boolean unreadOnly
    ) {
        UUID userId =
                UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                notificationService.getNotifications(
                        userId,
                        page,
                        Math.min(size, 50),
                        unreadOnly
                )
        );
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(
            Authentication authentication
    ) {
        UUID userId =
                UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                notificationService.getUnreadCount(userId)
        );
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable UUID notificationId,
            Authentication authentication
    ) {
        UUID userId =
                UUID.fromString(authentication.getName());

        notificationService.markAsRead(
                userId,
                notificationId
        );

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(
            Authentication authentication
    ) {
        UUID userId =
                UUID.fromString(authentication.getName());

        notificationService.markAllAsRead(userId);

        return ResponseEntity.noContent().build();
    }
}