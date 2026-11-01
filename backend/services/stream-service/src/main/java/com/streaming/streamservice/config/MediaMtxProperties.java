package com.streaming.streamservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "stream.infrastructure.media-mtx")
public class MediaMtxProperties {

    private String authSecret;
}