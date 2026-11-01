package com.streaming.followservice.controller;

import com.streaming.followservice.dto.response.FollowedChannelResponse;
import com.streaming.followservice.dto.response.PageResponse;
import com.streaming.followservice.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
@Validated
public class InternalFollowController {

    private final FollowService followService;

    @GetMapping("/channels/{channelId}/followers")
    public List<UUID> getFollowerIds(
            @PathVariable UUID channelId
    ) {
        return followService.getFollowerIds(channelId);
    }

    @GetMapping("/follows/user/{userId}")
    public PageResponse<FollowedChannelResponse> getFollowedChannels(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        return followService.getFollowedChannels(
                userId,
                page,
                size
        );
    }
}
