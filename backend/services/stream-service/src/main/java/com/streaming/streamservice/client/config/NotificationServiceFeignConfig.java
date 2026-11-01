package com.streaming.streamservice.client.config;

import com.streaming.streamservice.security.JwtService;
import feign.RequestInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
public class NotificationServiceFeignConfig {

    private final JwtService jwtService;

    @Bean
    public RequestInterceptor notificationServiceAuthInterceptor() {
        return requestTemplate -> {
            String serviceToken =
                    jwtService.generateServiceToken("NOTIFICATION-SERVICE");

            requestTemplate.header(
                    "Authorization",
                    "Bearer " + serviceToken
            );
        };
    }
}