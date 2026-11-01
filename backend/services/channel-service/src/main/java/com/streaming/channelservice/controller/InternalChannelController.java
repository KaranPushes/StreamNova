package com.streaming.channelservice.controller;

import com.streaming.channelservice.dto.client.InternalChannelResponse;
import com.streaming.channelservice.dto.request.UpdateChannelStatusRequest;
import com.streaming.channelservice.dto.response.ChannelResponse;
import com.streaming.channelservice.dto.response.ChannelSearchResponse;
import com.streaming.channelservice.dto.response.PageResponse;
import com.streaming.channelservice.entity.Channel;
import com.streaming.channelservice.exception.ChannelNotFoundException;
import com.streaming.channelservice.repository.ChannelRepository;
import com.streaming.channelservice.service.ChannelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/internal/channels")
@RequiredArgsConstructor
public class InternalChannelController {

    private final ChannelService channelService;
    private final ChannelRepository channelRepository;

    @PatchMapping("/{id}/status")
    public ChannelResponse updateChannelStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateChannelStatusRequest request) {

        return channelService.updateChannelStatus(
                id,
                request.status()
        );
    }

    @GetMapping("/{id}/owner/{userId}")
    public boolean isOwner(
            @PathVariable UUID id,
            @PathVariable UUID userId
    ) {
        return channelRepository
                .findById(id)
                .map(channel ->
                        channel.getOwnerUserId().equals(userId)
                )
                .orElse(false);
    }

    @GetMapping("/by-owner")
    public InternalChannelResponse getChannelByOwner(
            @RequestParam UUID ownerUserId
    ) {
        Channel channel = channelRepository.findByOwnerUserId(ownerUserId)
                .orElseThrow(() ->
                        new ChannelNotFoundException("Channel not found for user: " + ownerUserId)
                );

        return new InternalChannelResponse(
                channel.getId(),
                channel.getOwnerUserId(),
                channel.getSlug(),
                channel.getDisplayName(),
                channel.getStatus()
        );
    }

    @GetMapping("/{id}")
    public InternalChannelResponse getChannel(
            @PathVariable UUID id) {

        return channelService.getInternalChannel(id);
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

    @GetMapping("/active")
    public List<ChannelSearchResponse> getActiveChannels(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return channelService.getActiveChannels(page, size);
    }

    @GetMapping("/by-ids")
    public List<InternalChannelResponse> getChannelsByIds(
            @RequestParam("ids") List<UUID> ids
    ) {
        return channelService.getChannelsByIds(ids);
    }
}