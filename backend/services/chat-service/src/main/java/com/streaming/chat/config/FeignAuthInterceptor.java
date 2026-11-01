package com.streaming.chat.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;

@Component
public class FeignAuthInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {

        // JWT forwarding will be handled from the current
        // WebSocket session before the Feign call.
    }
}