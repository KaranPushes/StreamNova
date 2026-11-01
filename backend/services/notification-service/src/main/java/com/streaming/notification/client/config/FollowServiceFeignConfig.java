package com.streaming.notification.client.config;

import com.streaming.notification.security.JwtService;
import feign.RequestInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
public class FollowServiceFeignConfig {

    private final JwtService jwtService;

    @Bean
    public RequestInterceptor followServiceAuthInterceptor() {
        return requestTemplate -> {
            String serviceToken =
                    jwtService.generateServiceToken(
                            "NOTIFICATION-SERVICE"
                    );

            requestTemplate.header(
                    "Authorization",
                    "Bearer " + serviceToken
            );
        };
    }
}
