package com.streaming.mediaservice.controller;

import com.streaming.mediaservice.dto.response.MediaResponse;
import com.streaming.mediaservice.entity.MediaType;
import com.streaming.mediaservice.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;

    @PostMapping("/avatar")
    @ResponseStatus(HttpStatus.CREATED)
    public MediaResponse uploadAvatar(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam("file") MultipartFile file) {

        UUID userId = UUID.fromString(jwt.getSubject());

        return mediaService.upload(
                userId,
                MediaType.AVATAR,
                file
        );
    }

    @PostMapping("/banner")
    @ResponseStatus(HttpStatus.CREATED)
    public MediaResponse uploadBanner(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam("file") MultipartFile file) {

        UUID userId = UUID.fromString(jwt.getSubject());

        return mediaService.upload(
                userId,
                MediaType.BANNER,
                file
        );
    }

    @PostMapping("/stream-thumbnail")
    @ResponseStatus(HttpStatus.CREATED)
    public MediaResponse uploadStreamThumbnail(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam("file") MultipartFile file) {

        UUID userId = UUID.fromString(jwt.getSubject());

        return mediaService.upload(
                userId,
                MediaType.STREAM_THUMBNAIL,
                file
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMedia(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id) {

        UUID userId = UUID.fromString(jwt.getSubject());

        mediaService.delete(
                userId,
                id
        );
    }
}