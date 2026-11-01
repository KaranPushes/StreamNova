package com.streaming.streamservice.controller;

import com.streaming.streamservice.dto.request.CreateStreamRequest;
import com.streaming.streamservice.dto.request.ScheduleStreamRequest;
import com.streaming.streamservice.dto.request.UpdateStreamRequest;
import com.streaming.streamservice.dto.response.CreateStreamResponse;
import com.streaming.streamservice.dto.response.PageResponse;
import com.streaming.streamservice.dto.response.StreamResponse;
import com.streaming.streamservice.service.StreamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/streams")
@RequiredArgsConstructor
public class StreamController {

    private final StreamService streamService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateStreamResponse createStream(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateStreamRequest request
    ) {
        UUID ownerUserId = UUID.fromString(jwt.getSubject());

        return streamService.createStream(
                ownerUserId,
                request
        );
    }

    @PatchMapping("/{streamId}")
    public StreamResponse updateStream(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID streamId,
            @Valid @RequestBody UpdateStreamRequest request
    ) {
        UUID ownerUserId = UUID.fromString(jwt.getSubject());

        return streamService.updateStream(
                ownerUserId,
                streamId,
                request
        );
    }

    @PostMapping("/{streamId}/schedule")
    public StreamResponse scheduleStream(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID streamId,
            @Valid @RequestBody ScheduleStreamRequest request
    ) {
        UUID ownerUserId = UUID.fromString(jwt.getSubject());

        return streamService.scheduleStream(
                ownerUserId,
                streamId,
                request
        );
    }

    @GetMapping("/{streamId:[0-9a-fA-F-]{36}}/can-moderate/{userId}")
    public boolean canModerate(
            @PathVariable UUID streamId,
            @PathVariable UUID userId
    ) {
        return streamService.canModerate(
                userId,
                streamId
        );
    }

    @PostMapping("/{streamId}/start")
    public StreamResponse startStream(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID streamId
    ) {
        UUID ownerUserId = UUID.fromString(jwt.getSubject());

        return streamService.startStream(
                ownerUserId,
                streamId
        );
    }

    @PostMapping("/{streamId}/end")
    public StreamResponse endStream(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID streamId
    ) {
        UUID ownerUserId = UUID.fromString(jwt.getSubject());

        return streamService.endStream(
                ownerUserId,
                streamId
        );
    }

    @GetMapping("/{streamId:[0-9a-fA-F-]{36}}")
    public StreamResponse getStream(
            @PathVariable UUID streamId
    ) {
        return streamService.getStream(streamId);
    }

    @GetMapping("/channel/{channelId}")
    public List<StreamResponse> getChannelStreams(
            @PathVariable UUID channelId
    ) {
        return streamService.getChannelStreams(channelId);
    }

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
}