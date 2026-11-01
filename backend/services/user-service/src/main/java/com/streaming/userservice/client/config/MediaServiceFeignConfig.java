package com.streaming.userservice.client.config;

import com.streaming.userservice.constants.SecurityConstants;
import com.streaming.userservice.security.JwtService;
import feign.RequestInterceptor;
import lombok.RequiredArgsConstructor;
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
                            "USER-SERVICE"
                    );

            requestTemplate.header(
                    SecurityConstants.AUTHORIZATION_HEADER,
                    SecurityConstants.TOKEN_PREFIX + serviceToken
            );
        };
    }
}