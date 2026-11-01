package com.streaming.feed.client;

import com.streaming.feed.client.config.FollowServiceFeignConfig;
import com.streaming.feed.dto.FollowedChannelResponse;
import com.streaming.feed.dto.PageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(
        name = "FOLLOW-SERVICE",
        configuration = FollowServiceFeignConfig.class
)
public interface FollowServiceClient {

    @GetMapping("/internal/follows/user/{userId}")
    PageResponse<FollowedChannelResponse> getFollowedChannels(
            @PathVariable UUID userId,
            @RequestParam("page") int page,
            @RequestParam("size") int size
    );
}