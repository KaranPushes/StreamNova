package com.streaming.mediaservice.controller;

import com.streaming.mediaservice.entity.Media;
import com.streaming.mediaservice.exception.MediaNotFoundException;
import com.streaming.mediaservice.repository.MediaRepository;
import com.streaming.mediaservice.storage.ObjectStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/media/files")
@RequiredArgsConstructor
public class MediaFileController {

    private final MediaRepository mediaRepository;
    private final ObjectStorageService objectStorageService;

    @GetMapping("/{id}")
    public ResponseEntity<Resource> getMedia(
            @PathVariable UUID id) {

        Media media = mediaRepository
                .findById(id)
                .filter(findMedia ->
                        findMedia.getStatus().name().equals("ACTIVE")
                )
                .orElseThrow(() ->
                        new MediaNotFoundException(id)
                );

        Resource resource =
                objectStorageService.download(
                        media.getStorageKey()
                );

        MediaType contentType =
                MediaType.parseMediaType(
                        media.getContentType()
                );

        return ResponseEntity.ok()
                .contentType(contentType)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" +
                                media.getOriginalFileName() +
                                "\""
                )
                .body(resource);
    }

    @GetMapping("/{type}/{userId}/{filename}")
    public ResponseEntity<Resource> getMediaByKey(
            @PathVariable String type,
            @PathVariable UUID userId,
            @PathVariable String filename) {
            
        String key = type + "/" + userId + "/" + filename;
        Resource resource = objectStorageService.download(key);
        
        String extension = filename.substring(filename.lastIndexOf('.')).toLowerCase();
        MediaType contentType = MediaType.APPLICATION_OCTET_STREAM;
        if (extension.equals(".png")) contentType = MediaType.IMAGE_PNG;
        else if (extension.equals(".jpg") || extension.equals(".jpeg")) contentType = MediaType.IMAGE_JPEG;
        else if (extension.equals(".webp")) contentType = MediaType.parseMediaType("image/webp");

        return ResponseEntity.ok()
                .contentType(contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(resource);
    }
}