package com.streaming.notification.event;

import java.util.UUID;

public record NewFollowerEvent(
        UUID followedUserId,
        UUID followerUserId
) {
}