package com.streaming.notification.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

import com.streaming.notification.client.config.FollowServiceFeignConfig;

@FeignClient(name = "FOLLOW-SERVICE", configuration = FollowServiceFeignConfig.class)
public interface FollowServiceClient {

    @GetMapping("/internal/channels/{channelId}/followers")
    List<UUID> getFollowerIds(
            @PathVariable UUID channelId
    );
}