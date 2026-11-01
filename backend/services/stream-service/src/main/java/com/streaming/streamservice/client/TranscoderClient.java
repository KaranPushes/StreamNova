package com.streaming.streamservice.client;

import com.streaming.streamservice.dto.transcoder.StartTranscoderRequest;
import com.streaming.streamservice.dto.transcoder.StopTranscoderRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.streaming.streamservice.client.config.TranscoderServiceFeignConfig;

@FeignClient(name = "TRANSCODER-SERVICE", url = "${feign.client.transcoder.url:}", configuration = TranscoderServiceFeignConfig.class)
public interface TranscoderClient {

    @PostMapping("/internal/transcoders/start")
    void start(@RequestBody StartTranscoderRequest request);

    @PostMapping("/internal/transcoders/stop")
    void stop(@RequestBody StopTranscoderRequest request);
}