package com.streaming.feed.dto;

import java.util.List;

public record HomeFeedResponse(
        List<LiveStreamResponse> liveStreams,
        List<ChannelSearchResponse> channels,
        List<CategoryResponse> categories,
        List<RecommendedChannelResponse> recommendedChannels
) {}