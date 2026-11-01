package com.streaming.feed.dto;

import java.util.UUID;

public record ChannelLiveCountResponse(
        UUID channelId,
        long liveStreamCount
) {}