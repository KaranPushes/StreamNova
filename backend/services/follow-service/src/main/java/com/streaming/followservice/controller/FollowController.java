package com.streaming.followservice.controller;

import com.streaming.followservice.dto.response.*;
import com.streaming.followservice.security.CurrentUserProvider;
import com.streaming.followservice.service.FollowService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/follows")
@RequiredArgsConstructor
@Validated
public class FollowController {

    private final FollowService followService;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping("/channels/{channelId}")
    @ResponseStatus(HttpStatus.CREATED)
    public FollowResponse followChannel(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID channelId) {

        UUID userId =
                currentUserProvider.getUserId(jwt);

        return followService.followChannel(
                userId,
                channelId
        );
    }

    @DeleteMapping("/channels/{channelId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unfollowChannel(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID channelId) {

        UUID userId =
                currentUserProvider.getUserId(jwt);

        followService.unfollowChannel(
                userId,
                channelId
        );
    }

    @GetMapping("/channels/{channelId}/status")
    public FollowStatusResponse getFollowStatus(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID channelId) {

        UUID userId =
                currentUserProvider.getUserId(jwt);

        return followService.getFollowStatus(
                userId,
                channelId
        );
    }

    @GetMapping("/channels/{channelId}/count")
    public FollowerCountResponse getFollowerCount(
            @PathVariable UUID channelId) {

        return followService.getFollowerCount(channelId);
    }

    @GetMapping("/me")
    public PageResponse<FollowedChannelResponse> getFollowedChannels(
            @AuthenticationPrincipal Jwt jwt,

            @RequestParam(defaultValue = "0")
            @Min(0)
            int page,

            @RequestParam(defaultValue = "20")
            @Min(1)
            @Max(50)
            int size) {

        UUID userId =
                currentUserProvider.getUserId(jwt);

        return followService.getFollowedChannels(
                userId,
                page,
                size
        );
    }
}