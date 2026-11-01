package com.streaming.mediaservice.service.impl;

import com.streaming.mediaservice.dto.response.InternalMediaResponse;
import com.streaming.mediaservice.dto.response.MediaResponse;
import com.streaming.mediaservice.entity.Media;
import com.streaming.mediaservice.entity.MediaStatus;
import com.streaming.mediaservice.entity.MediaType;
import com.streaming.mediaservice.exception.MediaNotFoundException;
import com.streaming.mediaservice.repository.MediaRepository;
import com.streaming.mediaservice.service.MediaService;
import com.streaming.mediaservice.storage.ObjectStorageService;
import com.streaming.mediaservice.validation.MediaValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {

    private final MediaRepository mediaRepository;
    private final MediaValidator mediaValidator;
    private final ObjectStorageService objectStorageService;

    @Override
    @Transactional
    public MediaResponse upload(
            UUID ownerUserId,
            MediaType mediaType,
            MultipartFile file) {

        String detectedContentType =
                mediaValidator.validate(
                        file,
                        mediaType
                );

        UUID mediaId = UUID.randomUUID();

        String storageKey =
                buildStorageKey(
                        ownerUserId,
                        mediaType,
                        mediaId,
                        file.getOriginalFilename()
                );

        String url;

        try {

            url = objectStorageService.upload(
                    storageKey,
                    file.getInputStream(),
                    file.getSize(),
                    detectedContentType
            );

        } catch (IOException exception) {

            throw new IllegalStateException(
                    "Unable to read media file",
                    exception
            );
        }

        Media media = new Media();

        media.setId(mediaId);
        media.setOwnerUserId(ownerUserId);
        media.setMediaType(mediaType);
        media.setOriginalFileName(
                file.getOriginalFilename()
        );
        media.setStorageKey(storageKey);
        media.setUrl(url);
        media.setContentType(
                detectedContentType
        );
        media.setFileSize(file.getSize());
        media.setStatus(MediaStatus.ACTIVE);

        Media savedMedia =
                mediaRepository.save(media);

        return toResponse(savedMedia);
    }

    @Override
    @Transactional(readOnly = true)
    public InternalMediaResponse getInternalMedia(
            UUID ownerUserId,
            UUID mediaId,
            MediaType expectedType) {

        Media media = mediaRepository
                .findByIdAndOwnerUserIdAndStatus(
                        mediaId,
                        ownerUserId,
                        MediaStatus.ACTIVE
                )
                .orElseThrow(() ->
                        new MediaNotFoundException(mediaId)
                );

        if (media.getMediaType() != expectedType) {
            throw new IllegalArgumentException(
                    "Media type does not match expected type"
            );
        }

        return new InternalMediaResponse(
                media.getId(),
                media.getOwnerUserId(),
                media.getMediaType(),
                media.getUrl()
        );
    }

    @Override
    @Transactional
    public void delete(
            UUID ownerUserId,
            UUID mediaId) {

        Media media =
                mediaRepository
                        .findByIdAndOwnerUserIdAndStatus(
                                mediaId,
                                ownerUserId,
                                MediaStatus.ACTIVE
                        )
                        .orElseThrow(() ->
                            new MediaNotFoundException(mediaId)
                        );

        objectStorageService.delete(
                media.getStorageKey()
        );

        media.setStatus(MediaStatus.DELETED);

        mediaRepository.save(media);
    }

    private String buildStorageKey(
            UUID ownerUserId,
            MediaType mediaType,
            UUID mediaId,
            String originalFileName) {

        String extension =
                extractExtension(originalFileName);

        return mediaType.name().toLowerCase()
                + "/"
                + ownerUserId
                + "/"
                + mediaId
                + extension;
    }

    private String extractExtension(
            String filename) {

        if (filename == null) {
            return "";
        }

        int dotIndex =
                filename.lastIndexOf('.');

        if (dotIndex < 0 ||
                dotIndex == filename.length() - 1) {

            return "";
        }

        String extension =
                filename.substring(dotIndex)
                        .toLowerCase();

        return switch (extension) {
            case ".jpg", ".jpeg", ".png", ".webp" ->
                    extension;

            default -> "";
        };
    }

    private MediaResponse toResponse(
            Media media) {

        return new MediaResponse(
                media.getId(),
                media.getMediaType(),
                media.getOriginalFileName(),
                media.getUrl(),
                media.getContentType(),
                media.getFileSize(),
                media.getCreatedAt()
        );
    }
}