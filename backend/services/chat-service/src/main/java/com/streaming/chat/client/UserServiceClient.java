package com.streaming.chat.client;

import com.streaming.chat.client.config.UserServiceFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "user-service", 
            configuration = UserServiceFeignConfig.class)
public interface UserServiceClient {

    @GetMapping("/internal/users/{id}")
    UserResponse getInternalUserById(@PathVariable UUID id);

    record UserResponse(
            UUID id,
            String email,
            String username,
            String displayName,
            String avatarUrl,
            String role,
            String status
    ) {
    }
}
