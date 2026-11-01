package com.streaming.feed.service;

import com.streaming.feed.dto.*;

import java.util.List;
import java.util.UUID;

public interface FeedService {

    PageResponse<LiveStreamResponse> getLiveStreams(
            String category,
            String q,
            int page,
            int size,
            String sort
    );

    PageResponse<ChannelSearchResponse> searchChannels(
            String query,
            int page,
            int size
    );

    PersonalizedFeedResponse getPersonalizedFeed(
            UUID userId
    );

    HomeFeedResponse getHomeFeed();

    List<CategoryResponse> getCategories();

    LiveStreamResponse getStreamDetails(UUID streamId);

    DiscoverySearchResponse search(
            String query,
            int page,
            int size
    );
    PageResponse<RecommendedChannelResponse> getFollowedChannelsDetailed(
            UUID userId,
            int page,
            int size
    );
}