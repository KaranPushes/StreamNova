package com.streaming.followservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

import com.streaming.followservice.client.config.ChannelServiceFeignConfig;

@FeignClient(
    name = "NOTIFICATION-SERVICE", 
    configuration = ChannelServiceFeignConfig.class
)
public interface NotificationServiceClient {

    @PostMapping("/internal/notifications/events/follower")
    void notifyNewFollower(
            @RequestBody NewFollowerEvent event
    );

    record NewFollowerEvent(
            UUID followedUserId,
            UUID followerUserId
    ) {}
}