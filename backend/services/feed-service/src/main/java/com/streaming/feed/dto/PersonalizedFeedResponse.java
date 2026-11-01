package com.streaming.feed.dto;

import java.util.List;

public record PersonalizedFeedResponse(
        List<LiveStreamResponse> followedStreams,
        List<LiveStreamResponse> recommendedStreams,
        List<RecommendedChannelResponse> recommendedChannels
) {}