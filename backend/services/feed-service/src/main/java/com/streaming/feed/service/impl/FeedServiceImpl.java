package com.streaming.feed.service.impl;

import com.streaming.feed.client.ChannelServiceClient;
import com.streaming.feed.client.FollowServiceClient;
import com.streaming.feed.client.StreamServiceClient;
import com.streaming.feed.dto.*;
import com.streaming.feed.exception.StreamNotLiveException;
import com.streaming.feed.service.FeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedServiceImpl implements FeedService {

    private final StreamServiceClient streamServiceClient;
    private final ChannelServiceClient channelServiceClient;
    private final FollowServiceClient followServiceClient;

    @Override
    public HomeFeedResponse getHomeFeed() {

        List<ChannelSearchResponse> channels =
                channelServiceClient.getActiveChannels(0, 20);

        PageResponse<LiveStreamResponse> livePage =
                getHomeLiveStreams();

        List<CategoryResponse> categories =
                getCategories();

        List<RecommendedChannelResponse> recommendedChannels =
                getRecommendedChannels();

        return new HomeFeedResponse(
                livePage.content(),
                channels,
                categories,
                recommendedChannels
        );
    }

    private List<RecommendedChannelResponse> getRecommendedChannels() {

        List<ChannelLiveCountResponse> counts =
                streamServiceClient.getLiveStreamCountsByChannel();

        if (counts.isEmpty()) {
            return List.of();
        }

        List<UUID> channelIds = counts.stream()
                .map(ChannelLiveCountResponse::channelId)
                .toList();

        Map<UUID, ChannelResponse> channels =
                channelServiceClient.getChannelsByIds(channelIds)
                        .stream()
                        .collect(Collectors.toMap(
                                ChannelResponse::id,
                                channel -> channel
                        ));

        return counts.stream()
                .map(count -> {
                    ChannelResponse channel =
                            channels.get(count.channelId());

                    if (channel == null) {
                        return null;
                    }

                    return new RecommendedChannelResponse(
                            channel.id(),
                            channel.slug(),
                            channel.displayName(),
                            channel.avatarUrl(),
                            count.liveStreamCount()
                    );
                })
                .filter(java.util.Objects::nonNull)
                .limit(10)
                .toList();
    }

    @Override
    public DiscoverySearchResponse search(
            String query,
            int page,
            int size
    ) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException(
                    "Search query must not be blank"
            );
        }

        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page must not be negative"
            );
        }

        if (size < 1 || size > 50) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and 50"
            );
        }

        String normalizedQuery = query.trim();

        PageResponse<ChannelSearchResponse> channels =
                searchChannels(
                        normalizedQuery,
                        page,
                        size
                );

        PageResponse<LiveStreamResponse> streams =
                getLiveStreams(
                        null,
                        normalizedQuery,
                        page,
                        size,
                        "recent"
                );

        List<CategoryResponse> categories =
                getCategories()
                        .stream()
                        .filter(category ->
                                category.name() != null
                                        && category.name()
                                        .toLowerCase()
                                        .contains(
                                                normalizedQuery.toLowerCase()
                                        )
                        )
                        .toList();

        return new DiscoverySearchResponse(
                channels.content(),
                streams.content(),
                categories
        );
    }

    @Override
    public PersonalizedFeedResponse getPersonalizedFeed(
            UUID userId
    ) {
        PageResponse<FollowedChannelResponse> followedPage =
                followServiceClient.getFollowedChannels(
                        userId,
                        0,
                        50
                );

        List<UUID> followedChannelIds =
                followedPage.content()
                        .stream()
                        .map(FollowedChannelResponse::channelId)
                        .toList();

        if (followedChannelIds.isEmpty()) {
            PageResponse<LiveStreamResponse> livePage =
                    getHomeLiveStreams();

            return new PersonalizedFeedResponse(
                    List.of(),
                    livePage.content(),
                    getRecommendedChannels()
            );
        }

        PageResponse<StreamResponse> followedStreamPage =
                streamServiceClient.getLiveStreamsByChannels(
                        followedChannelIds,
                        0,
                        20,
                        "recent"
                );

        List<StreamResponse> followedStreams =
                followedStreamPage.content();

        List<UUID> channelIds = followedStreams.stream()
                .map(StreamResponse::channelId)
                .distinct()
                .toList();

        Map<UUID, ChannelResponse> channels =
                channelServiceClient.getChannelsByIds(channelIds)
                        .stream()
                        .collect(Collectors.toMap(
                                ChannelResponse::id,
                                channel -> channel
                        ));

        List<LiveStreamResponse> followed =
                followedStreams.stream()
                        .map(stream ->
                                toResponse(
                                        stream,
                                        channels.get(stream.channelId())
                                )
                        )
                        .toList();

        PageResponse<LiveStreamResponse> recommendedPage =
                getHomeLiveStreams();

        List<LiveStreamResponse> recommended =
                recommendedPage.content()
                        .stream()
                        .filter(stream ->
                                !followedChannelIds.contains(
                                        stream.channelId()
                                )
                        )
                        .toList();

        return new PersonalizedFeedResponse(
                followed,
                recommended,
                getRecommendedChannels()
        );
    }

    @Override
    public PageResponse<ChannelSearchResponse> searchChannels(
            String query,
            int page,
            int size
    ) {
        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page must not be negative"
            );
        }

        if (size < 1 || size > 50) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and 50"
            );
        }

        if (query == null || query.isBlank()) {
            return new PageResponse<>(
                    List.of(),
                    page,
                    size,
                    0,
                    0,
                    true,
                    true
            );
        }

        return channelServiceClient.searchChannels(
                query.trim(),
                page,
                size
        );
    }

    @Override
    public PageResponse<LiveStreamResponse> getLiveStreams(
            String category,
            String q,
            int page,
            int size,
            String sort
    ) {


        if (!"recent".equalsIgnoreCase(sort)
                && !"oldest".equalsIgnoreCase(sort)) {
            throw new IllegalArgumentException(
                    "Unsupported sort: " + sort
            );
        }

        if (page < 0) {
            throw new IllegalArgumentException("Page must not be negative");
        }

        if (size < 1 || size > 50) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and 50"
            );
        }

        List<UUID> matchingChannelIds = List.of();

        if (q != null && !q.isBlank()) {
            PageResponse<ChannelSearchResponse> channelPage =
                    channelServiceClient.searchChannels(
                            q.trim(),
                            0,
                            50
                    );

            matchingChannelIds = channelPage.content()
                    .stream()
                    .map(ChannelSearchResponse::id)
                    .toList();
        }

        PageResponse<StreamResponse> streamPage =
                streamServiceClient.getLiveStreams(
                        page,
                        size,
                        category,
                        q,
                        matchingChannelIds,
                        sort
                );

        List<StreamResponse> streams =
                streamPage.content();

        List<UUID> channelIds = streams.stream()
                .map(StreamResponse::channelId)
                .distinct()
                .toList();

        Map<UUID, ChannelResponse> channels =
                channelServiceClient.getChannelsByIds(channelIds)
                        .stream()
                        .collect(Collectors.toMap(
                                ChannelResponse::id,
                                channel -> channel
                        ));

        List<LiveStreamResponse> content =
                streams.stream()
                        .map(stream ->
                                toResponse(
                                        stream,
                                        channels.get(stream.channelId())
                                )
                        )
                        .toList();

        return new PageResponse<>(
                content,
                streamPage.page(),
                streamPage.size(),
                streamPage.totalElements(),
                streamPage.totalPages(),
                streamPage.first(),
                streamPage.last()
        );
    }

    @Override
    public List<CategoryResponse> getCategories() {
        return streamServiceClient.getLiveStreamCategories();
    }

    @Override
    public LiveStreamResponse getStreamDetails(UUID streamId) {

        StreamResponse stream =
                streamServiceClient.getStream(streamId);

        if (!"LIVE".equalsIgnoreCase(stream.status())) {
            throw new StreamNotLiveException(
                    "Stream is not currently live"
            );
        }

        ChannelResponse channel =
                channelServiceClient.getChannelById(
                        stream.channelId()
                );

        return toResponse(
                stream,
                channel
        );
    }

    private LiveStreamResponse toResponse(
            StreamResponse stream,
            ChannelResponse channel
    ) {
        return new LiveStreamResponse(
                stream.id(),
                stream.channelId(),
                channel.slug(),
                channel.displayName(),
                channel.description(),
                channel.avatarUrl(),
                channel.bannerUrl(),
                stream.title(),
                stream.description(),
                stream.category(),
                stream.thumbnailUrl(),
                stream.startedAt(),
                stream.playbackUrl()
        );
    }

    private PageResponse<LiveStreamResponse> getHomeLiveStreams() {
        return getLiveStreams(
                null,
                null,
                0,
                20,
                "recent"
        );
    }

    @Override
    public PageResponse<RecommendedChannelResponse> getFollowedChannelsDetailed(UUID userId, int page, int size) {
        PageResponse<FollowedChannelResponse> followedPage =
                followServiceClient.getFollowedChannels(userId, page, size);

        List<UUID> channelIds = followedPage.content().stream()
                .map(FollowedChannelResponse::channelId)
                .toList();

        if (channelIds.isEmpty()) {
            return new PageResponse<>(List.of(), page, size, 0, 0, true, true);
        }

        Map<UUID, ChannelResponse> channelMap =
                channelServiceClient.getChannelsByIds(channelIds)
                        .stream()
                        .collect(Collectors.toMap(ChannelResponse::id, c -> c));

        // Get live streams for these channels to determine live status
        PageResponse<StreamResponse> liveStreams =
                streamServiceClient.getLiveStreamsByChannels(channelIds, 0, 50, "recent");
        List<UUID> liveChannelIds = liveStreams.content().stream()
                .map(StreamResponse::channelId)
                .toList();

        List<RecommendedChannelResponse> detailedChannels = followedPage.content().stream()
                .map(follow -> {
                    ChannelResponse channel = channelMap.get(follow.channelId());
                    if (channel == null) return null;
                    long isLiveCount = liveChannelIds.contains(channel.id()) ? 1L : 0L;
                    return new RecommendedChannelResponse(
                            channel.id(),
                            channel.slug(),
                            channel.displayName(),
                            channel.avatarUrl(),
                            isLiveCount
                    );
                })
                .filter(java.util.Objects::nonNull)
                .toList();

        return new PageResponse<>(
                detailedChannels,
                followedPage.page(),
                followedPage.size(),
                followedPage.totalElements(),
                followedPage.totalPages(),
                followedPage.first(),
                followedPage.last()
        );
    }
}