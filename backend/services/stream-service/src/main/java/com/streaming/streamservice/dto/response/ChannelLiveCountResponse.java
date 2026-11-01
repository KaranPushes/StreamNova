package com.streaming.streamservice.dto.response;

import java.util.UUID;

public record ChannelLiveCountResponse(
        UUID channelId,
        long liveStreamCount
) {}