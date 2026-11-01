package com.streaming.notification.event;

import com.streaming.notification.client.FollowServiceClient;
import com.streaming.notification.entity.NotificationType;
import com.streaming.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationEventService {

    private final NotificationService notificationService;
    private final FollowServiceClient followServiceClient;

    public void handleNewFollower(
            NewFollowerEvent event
    ) {
        notificationService.create(
                event.followedUserId(),
                NotificationType.FOLLOWER,
                "New follower",
                "You have a new follower.",
                event.followerUserId()
        );
    }

    public void handleStreamLive(StreamLiveEvent event) {

        List<UUID> followerIds =
                followServiceClient.getFollowerIds(
                        event.channelId()
                );

        for (UUID followerId : followerIds) {

            notificationService.create(
                    followerId,
                    NotificationType.STREAM_LIVE,
                    "Stream is live",
                    event.streamTitle() + " is now live.",
                    event.streamId()
            );
        }
    }

    public void handleStreamScheduled(StreamScheduledEvent event) {
        List<UUID> followerIds =
                followServiceClient.getFollowerIds(event.channelId());

        for (UUID followerId : followerIds) {
            notificationService.create(
                    followerId,
                    NotificationType.STREAM_SCHEDULED,
                    "Stream scheduled",
                    event.streamTitle() + " is scheduled to start at "
                            + event.scheduledAt(),
                    event.streamId()
            );
        }
    }

    public void handleSecurityNotification(
            SecurityNotificationEvent event) {

        notificationService.create(
                event.userId(),
                NotificationType.SECURITY,
                event.title(),
                event.message(),
                null
        );
    }
}