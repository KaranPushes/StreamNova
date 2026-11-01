package com.streaming.channelservice.client.config;

import com.streaming.channelservice.security.JwtService;
import lombok.RequiredArgsConstructor;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
public class MediaServiceFeignConfig {

    private final JwtService jwtService;

    @Bean
    public RequestInterceptor mediaServiceAuthInterceptor() {

        return requestTemplate -> {

            String serviceToken =
                    jwtService.generateServiceToken(
                            "CHANNEL-SERVICE"
                    );

            requestTemplate.header(
                    "Authorization",
                    "Bearer " + serviceToken
            );
        };
    }
}