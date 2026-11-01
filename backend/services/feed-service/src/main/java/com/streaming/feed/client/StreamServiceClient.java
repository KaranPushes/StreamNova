package com.streaming.feed.client;

import com.streaming.feed.client.config.StreamServiceFeignConfig;
import com.streaming.feed.dto.CategoryResponse;
import com.streaming.feed.dto.ChannelLiveCountResponse;
import com.streaming.feed.dto.PageResponse;
import com.streaming.feed.dto.StreamResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@FeignClient(
        name = "STREAM-SERVICE",
        configuration = StreamServiceFeignConfig.class
)
public interface StreamServiceClient {

    @GetMapping("/internal/streams/live")
    PageResponse<StreamResponse> getLiveStreams(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "channelIds", required = false) List<UUID> channelIds,
            @RequestParam("sort") String sort
    );

    @GetMapping("/internal/streams/live/channel-counts")
    List<ChannelLiveCountResponse> getLiveStreamCountsByChannel();

    @GetMapping("/internal/streams/live/by-channels")
    PageResponse<StreamResponse> getLiveStreamsByChannels(
            @RequestParam("channelIds") List<UUID> channelIds,
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam("sort") String sort
    );

    @GetMapping("/internal/streams/categories")
    List<CategoryResponse> getLiveStreamCategories();

    @GetMapping("/internal/streams/{id}")
    StreamResponse getStream(
            @PathVariable("id") UUID id
    );
}