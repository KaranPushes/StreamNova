package com.streaming.channelservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;


@Setter
@Getter
@ConfigurationProperties(prefix = "application.security.jwt")
public class JwtProperties {

    private String secretKey;

}