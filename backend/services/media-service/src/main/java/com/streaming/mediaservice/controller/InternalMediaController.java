package com.streaming.mediaservice.controller;

import com.streaming.mediaservice.dto.response.InternalMediaResponse;
import com.streaming.mediaservice.entity.MediaType;
import com.streaming.mediaservice.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/internal/media")
@RequiredArgsConstructor
public class InternalMediaController {

    private final MediaService mediaService;

    @GetMapping("/{id}")
    public InternalMediaResponse getMedia(
            @PathVariable UUID id,
            @RequestParam MediaType expectedType,
            @RequestParam UUID ownerUserId) {

        return mediaService.getInternalMedia(
                ownerUserId,
                id,
                expectedType
        );
    }
}