package com.streaming.followservice.client.config;

import com.streaming.followservice.client.decoder.ChannelServiceErrorDecoder;
import com.streaming.followservice.security.ServiceTokenProvider;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;

public class ChannelServiceFeignConfig {

    @Bean
    public RequestInterceptor serviceAuthInterceptor(
            ServiceTokenProvider serviceTokenProvider) {

        return requestTemplate -> {

            String token =
                    serviceTokenProvider.generateServiceToken();

            requestTemplate.header(
                    "Authorization",
                    "Bearer " + token
            );
        };
    }

    @Bean
    public ChannelServiceErrorDecoder channelServiceErrorDecoder() {
        return new ChannelServiceErrorDecoder();
    }
}