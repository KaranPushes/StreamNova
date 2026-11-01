package com.streaming.channelservice.controller;

import com.streaming.channelservice.dto.request.CreateChannelRequest;
import com.streaming.channelservice.dto.request.UpdateAvatarRequest;
import com.streaming.channelservice.dto.request.UpdateBannerRequest;
import com.streaming.channelservice.dto.request.UpdateChannelRequest;
import com.streaming.channelservice.dto.response.*;
import com.streaming.channelservice.security.CurrentUserProvider;
import com.streaming.channelservice.service.ChannelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/channels")
@RequiredArgsConstructor
@Validated
public class ChannelController {

    private final ChannelService channelService;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ChannelResponse createChannel(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateChannelRequest request) {

        UUID userId =
                currentUserProvider.getUserId(jwt);

        return channelService.createChannel(
                userId,
                request
        );
    }

    @GetMapping("/me")
    public ChannelResponse getCurrentChannel(
            @AuthenticationPrincipal Jwt jwt) {

        UUID userId =
                currentUserProvider.getUserId(jwt);

        return channelService.getCurrentChannel(userId);
    }

    @PatchMapping("/me")
    public ChannelResponse updateChannel(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateChannelRequest request) {

        UUID userId =
                currentUserProvider.getUserId(jwt);

        return channelService.updateChannel(
                userId,
                request
        );
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivateChannel(
            @AuthenticationPrincipal Jwt jwt) {

        UUID userId =
                currentUserProvider.getUserId(jwt);

        channelService.deactivateChannel(userId);
    }

    @GetMapping("/availability")
    public SlugAvailabilityResponse checkSlugAvailability(
            @RequestParam String slug) {

        return channelService.checkSlugAvailability(slug);
    }

    @GetMapping("/profile/{slug}")
    public PublicChannelResponse getPublicChannel(
            @PathVariable String slug) {

        return channelService.getPublicChannel(slug);
    }

    @PostMapping("/me/reactivate")
    public ChannelResponse reactivateChannel(
            @AuthenticationPrincipal Jwt jwt) {

        UUID userId =
                currentUserProvider.getUserId(jwt);

        return channelService.reactivateChannel(userId);
    }

    @PatchMapping("/me/avatar")
    public ChannelResponse updateAvatar(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateAvatarRequest request) {

        UUID userId =
                currentUserProvider.getUserId(jwt);

        return channelService.updateAvatar(
                userId,
                request
        );
    }

    @DeleteMapping("/me/avatar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAvatar(
            @AuthenticationPrincipal Jwt jwt) {

        UUID userId =
                currentUserProvider.getUserId(jwt);

        channelService.deleteAvatar(userId);
    }

    @PatchMapping("/me/banner")
    public ChannelResponse updateBanner(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateBannerRequest request) {

        UUID userId =
                currentUserProvider.getUserId(jwt);

        return channelService.updateBanner(
                userId,
                request
        );
    }

    @DeleteMapping("/me/banner")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBanner(
            @AuthenticationPrincipal Jwt jwt) {

        UUID userId =
                currentUserProvider.getUserId(jwt);

        channelService.deleteBanner(userId);
    }

    @GetMapping("/search")
    public PageResponse<ChannelSearchResponse> searchChannels(
            @RequestParam("q") String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return channelService.searchChannels(
                query,
                page,
                size
        );
    }
}