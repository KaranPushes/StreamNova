package com.streaming.streamservice.controller;

import com.streaming.streamservice.dto.request.StreamAuthenticationRequest;
import com.streaming.streamservice.dto.response.CategoryResponse;
import com.streaming.streamservice.dto.response.ChannelLiveCountResponse;
import com.streaming.streamservice.dto.response.PageResponse;
import com.streaming.streamservice.dto.response.StreamResponse;
import com.streaming.streamservice.service.StreamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/internal/streams")
@RequiredArgsConstructor
public class InternalStreamController {

    private final StreamService streamService;

    @GetMapping("/live")
    public PageResponse<StreamResponse> getLiveStreamsInternal(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) List<UUID> channelIds,
            @RequestParam(defaultValue = "recent") String sort
    ) {
        return streamService.getLiveStreams(
                page,
                size,
                category,
                query,
                channelIds,
                sort
        );
    }

    @GetMapping("/live/by-channels")
    public PageResponse<StreamResponse> getLiveStreamsByChannels(
            @RequestParam List<UUID> channelIds,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "recent") String sort
    ) {
        return streamService.getLiveStreamsByChannels(
                channelIds,
                page,
                size,
                sort
        );
    }

    @GetMapping("/live/channel-counts")
    public List<ChannelLiveCountResponse> getLiveStreamCountsByChannel() {
        return streamService.getLiveStreamCountsByChannel();
    }

    @GetMapping("/categories")
    public List<CategoryResponse> getLiveStreamCategories() {
        return streamService.getLiveStreamCategories();
    }

    @PostMapping("/authenticate")
    public ResponseEntity<Void> authenticate(
            @Valid @RequestBody StreamAuthenticationRequest request
    ) {
        streamService.authenticateStreamKey(request);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{streamKey}/publish")
    public ResponseEntity<Void> publish(
            @PathVariable String streamKey
    ) {
        streamService.handleStreamPublish(streamKey);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{streamKey}/unpublish")
    public ResponseEntity<Void> unpublish(
            @PathVariable String streamKey
    ) {
        streamService.handleStreamUnpublish(streamKey);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public StreamResponse getStream(
            @PathVariable UUID id
    ) {
        return streamService.getStream(id);
    }
}