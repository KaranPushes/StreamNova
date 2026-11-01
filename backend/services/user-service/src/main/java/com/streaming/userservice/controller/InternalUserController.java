package com.streaming.userservice.controller;

import com.streaming.userservice.dto.request.CreateUserRequest;
import com.streaming.userservice.dto.request.UpdateUserStatusRequest;
import com.streaming.userservice.dto.response.UserResponse;
import com.streaming.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserService userService;

    @PostMapping
    public UserResponse createInternalUser(
            @Valid @RequestBody CreateUserRequest request) {

        return userService.createUser(request);
    }

    @GetMapping
    public UserResponse getUserByEmail(
            @RequestParam String email) {

        return userService.getUserByEmail(email);
    }

    @GetMapping("/{id}")
    public UserResponse getInternalUserById(
            @PathVariable UUID id) {

        return userService.getInternalUserById(id);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<UserResponse> updateUserStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserStatusRequest request
    ) {
        UserResponse response =
                userService.updateUserStatus(id, request);

        return ResponseEntity.ok(response);
    }
}
