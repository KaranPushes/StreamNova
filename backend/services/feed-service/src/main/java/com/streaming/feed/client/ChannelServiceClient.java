package com.streaming.feed.client;

import com.streaming.feed.client.config.ChannelServiceFeignConfig;
import com.streaming.feed.dto.ChannelResponse;
import com.streaming.feed.dto.ChannelSearchResponse;
import com.streaming.feed.dto.PageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@FeignClient(
        name = "CHANNEL-SERVICE",
        configuration = ChannelServiceFeignConfig.class
)
public interface ChannelServiceClient {

    @GetMapping("/internal/channels/{id}")
    ChannelResponse getChannelById(
            @PathVariable("id") UUID id
    );

    @GetMapping("/internal/channels/search")
    PageResponse<ChannelSearchResponse> searchChannels(
            @RequestParam("q") String query,
            @RequestParam("page") int page,
            @RequestParam("size") int size
    );

    @GetMapping("/internal/channels/by-ids")
    List<ChannelResponse> getChannelsByIds(
            @RequestParam("ids") List<UUID> ids
    );

    @GetMapping("/internal/channels/active")
    List<ChannelSearchResponse> getActiveChannels(
            @RequestParam("page") int page,
            @RequestParam("size") int size
    );
}