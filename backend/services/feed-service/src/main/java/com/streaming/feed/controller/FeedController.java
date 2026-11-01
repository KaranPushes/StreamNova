package com.streaming.feed.controller;

import com.streaming.feed.dto.*;
import com.streaming.feed.service.FeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/feed")
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    @GetMapping("/home")
    public HomeFeedResponse getHomeFeed() {
        return feedService.getHomeFeed();
    }

    @GetMapping("/live")
    public PageResponse<LiveStreamResponse> getLiveStreams(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "recent") String sort
    ) {
        return feedService.getLiveStreams(
                category,
                q,
                page,
                size,
                sort
        );
    }

    @GetMapping("/channels")
    public PageResponse<ChannelSearchResponse> searchChannels(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return feedService.searchChannels(
                q,
                page,
                size
        );
    }

    @GetMapping("/categories")
    public List<CategoryResponse> getCategories() {
        return feedService.getCategories();
    }

    @GetMapping("/categories/{category}")
    public PageResponse<LiveStreamResponse> getCategoryStreams(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return feedService.getLiveStreams(
                category,
                null,
                page,
                size,
                "recent"
        );
    }

    @GetMapping("/personalized")
    public PersonalizedFeedResponse getPersonalizedFeed(
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(
                jwt.getSubject()
        );

        return feedService.getPersonalizedFeed(userId);
    }

    @GetMapping("/streams/{streamId}")
    public LiveStreamResponse getStreamDetails(
            @PathVariable UUID streamId
    ) {
        return feedService.getStreamDetails(streamId);
    }

    @GetMapping("/search")
    public DiscoverySearchResponse search(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return feedService.search(q, page, size);
    }

    @GetMapping("/following")
    public PageResponse<RecommendedChannelResponse> getDetailedFollowing(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return feedService.getFollowedChannelsDetailed(userId, page, size);
    }
}