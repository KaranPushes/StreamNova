package com.streaming.streamservice.client;

import com.streaming.streamservice.client.config.MediaServiceFeignConfig;
import com.streaming.streamservice.dto.client.InternalMediaResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(
        name = "MEDIA-SERVICE",
        configuration = MediaServiceFeignConfig.class
)
public interface MediaServiceClient {

    @GetMapping("/internal/media/{id}")
    InternalMediaResponse getMedia(
            @PathVariable("id") UUID id,
            @RequestParam("expectedType") String expectedType,
            @RequestParam("ownerUserId") UUID ownerUserId
    );
}