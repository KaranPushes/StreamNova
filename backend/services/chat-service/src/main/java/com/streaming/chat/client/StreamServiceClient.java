package com.streaming.chat.client;

import com.streaming.chat.client.config.StreamServiceFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;


@FeignClient(name = "STREAM-SERVICE", configuration = StreamServiceFeignConfig.class)
public interface StreamServiceClient {

    @GetMapping("/streams/{streamId}")
    StreamResponse getStream(
            @PathVariable UUID streamId
    );

    @GetMapping("/streams/{streamId}/can-moderate/{userId}")
    boolean canModerate(
            @PathVariable UUID streamId,
            @PathVariable UUID userId
    );

    record StreamResponse(
            UUID id,
            String status
    ) {
    }
}