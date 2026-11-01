package com.streaming.feed.client.config;

import com.streaming.feed.security.JwtService;
import com.streaming.feed.constants.SecurityConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import feign.RequestInterceptor;

@RequiredArgsConstructor
public class FollowServiceFeignConfig {

    private final JwtService jwtService;

    @Bean
    public RequestInterceptor followServiceAuthInterceptor() {
        return requestTemplate -> {

            String serviceToken =
                    jwtService.generateServiceToken("FEED-SERVICE");

            requestTemplate.header(
                    SecurityConstants.AUTHORIZATION_HEADER,
                    SecurityConstants.TOKEN_PREFIX + serviceToken
            );
        };
    }
}