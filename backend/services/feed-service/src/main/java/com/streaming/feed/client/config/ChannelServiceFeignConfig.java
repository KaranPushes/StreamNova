package com.streaming.feed.client.config;

import com.streaming.feed.constants.SecurityConstants;
import com.streaming.feed.security.JwtService;
import feign.RequestInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
public class ChannelServiceFeignConfig {

    private final JwtService jwtService;

    @Bean
    public RequestInterceptor channelServiceAuthInterceptor() {

        return requestTemplate -> {

            String serviceToken =
                    jwtService.generateServiceToken(
                            "FEED-SERVICE"
                    );

            requestTemplate.header(
                    SecurityConstants.AUTHORIZATION_HEADER,
                    SecurityConstants.TOKEN_PREFIX + serviceToken
            );
        };
    }
}