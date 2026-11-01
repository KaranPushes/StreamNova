package com.streaming.followservice.client;

import com.streaming.followservice.client.config.ChannelServiceFeignConfig;
import com.streaming.followservice.dto.client.ChannelResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(
        name = "CHANNEL-SERVICE",
        configuration = ChannelServiceFeignConfig.class
)
public interface ChannelServiceClient {

    @GetMapping("/internal/channels/{id}")
    ChannelResponse getChannelById(
            @PathVariable("id") UUID channelId
    );
}