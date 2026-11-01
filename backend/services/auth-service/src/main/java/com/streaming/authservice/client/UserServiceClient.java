package com.streaming.authservice.client;

import com.streaming.authservice.client.config.UserServiceFeignConfig;
import com.streaming.authservice.dto.client.CreateUserRequest;
import com.streaming.authservice.dto.client.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(
        name = "USER-SERVICE",
        configuration = UserServiceFeignConfig.class
)
public interface UserServiceClient {

    @PostMapping("/internal/users")
    UserResponse createUser(@RequestBody CreateUserRequest request);

    @GetMapping("/internal/users")
    UserResponse getUserByEmail(@RequestParam String email);

    @GetMapping("/internal/users/{id}")
    UserResponse getUserById(@PathVariable("id") UUID id);

}