package com.streaming.streamservice.client;

import com.streaming.streamservice.client.config.NotificationServiceFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.Instant;
import java.util.UUID;

@FeignClient(name = "NOTIFICATION-SERVICE", configuration = NotificationServiceFeignConfig.class)
public interface NotificationServiceClient {

    @PostMapping("/internal/notifications/events/stream-live")
    void notifyStreamLive(
            @RequestBody StreamLiveEvent event
    );

    @PostMapping("/internal/notifications/events/stream-scheduled")
    void notifyStreamScheduled(
            @RequestBody StreamScheduledEvent event
    );

    record StreamLiveEvent(
            UUID streamId,
            UUID channelId,
            String streamTitle
    ) {}

    record StreamScheduledEvent(
            UUID streamId,
            UUID channelId,
            String streamTitle,
            Instant scheduledAt
    ) {}
}