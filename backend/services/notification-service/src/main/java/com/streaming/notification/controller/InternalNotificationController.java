package com.streaming.notification.controller;

import com.streaming.notification.event.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/notifications")
@RequiredArgsConstructor
public class InternalNotificationController {

    private final NotificationEventService notificationEventService;

    @PostMapping("/events/follower")
    public ResponseEntity<Void> newFollower(
            @RequestBody NewFollowerEvent event
    ) {
        notificationEventService.handleNewFollower(event);

        return ResponseEntity.accepted().build();
    }

    @PostMapping("/events/stream-live")
    public ResponseEntity<Void> streamLive(
            @RequestBody StreamLiveEvent event
    ) {
        notificationEventService.handleStreamLive(event);

        return ResponseEntity.accepted().build();
    }

    @PostMapping("/events/stream-scheduled")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void streamScheduled(
            @RequestBody StreamScheduledEvent event) {

        notificationEventService.handleStreamScheduled(event);
    }

    @PostMapping("/events/security")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void securityNotification(
            @RequestBody SecurityNotificationEvent event) {

        notificationEventService.handleSecurityNotification(event);
    }
}