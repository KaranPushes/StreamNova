package com.streaming.chat.client.config;

import com.streaming.chat.security.JwtService;
import lombok.RequiredArgsConstructor;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
public class UserServiceFeignConfig {

    private final JwtService jwtService;

    @Bean
    public RequestInterceptor userServiceAuthInterceptor() {

        return requestTemplate -> {

            String serviceToken =
                    jwtService.generateServiceToken(
                            "CHAT-SERVICE"
                    );

            requestTemplate.header(
                    "Authorization",
                    "Bearer " + serviceToken
            );
        };
    }
}
