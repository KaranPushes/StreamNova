package com.streaming.streamservice.client;

import com.streaming.streamservice.client.config.ChannelServiceFeignConfig;
import com.streaming.streamservice.dto.client.InternalChannelResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(
        name = "CHANNEL-SERVICE",
        configuration = ChannelServiceFeignConfig.class
)
public interface ChannelServiceClient {

    @GetMapping("/internal/channels/by-owner")
    InternalChannelResponse getChannelByOwner(
            @RequestParam("ownerUserId") UUID ownerUserId
    );

    @GetMapping("/internal/channels/{channelId}/owner/{userId}")
    boolean isOwner(
            @PathVariable UUID channelId,
            @PathVariable UUID userId
    );
}