package com.streaming.feed.dto;

import java.util.List;

public record DiscoverySearchResponse(
        List<ChannelSearchResponse> channels,
        List<LiveStreamResponse> streams,
        List<CategoryResponse> categories
) {}