package com.streaming.authservice.client.config;

import com.streaming.authservice.client.decoder.UserServiceErrorDecoder;
import com.streaming.authservice.constants.SecurityConstants;
import com.streaming.authservice.security.JwtService;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

public class UserServiceFeignConfig {

    @Bean
    public RequestInterceptor userServiceAuthInterceptor(
            JwtService jwtService) {

        return requestTemplate -> {

            String serviceToken =
                    jwtService.generateServiceToken("AUTH-SERVICE");

            requestTemplate.header(
                    SecurityConstants.AUTHORIZATION_HEADER,
                    SecurityConstants.TOKEN_PREFIX + serviceToken
            );
        };
    }

    @Bean
    public UserServiceErrorDecoder errorDecoder() {
        return new UserServiceErrorDecoder();
    }
}