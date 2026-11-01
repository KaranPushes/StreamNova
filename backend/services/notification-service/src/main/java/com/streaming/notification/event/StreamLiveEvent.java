package com.streaming.notification.event;

import java.util.UUID;

public record StreamLiveEvent(
        UUID streamId,
        UUID channelId,
        String streamTitle
) {
}