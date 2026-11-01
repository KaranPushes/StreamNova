package com.streaming.mediaservice.service;

import com.streaming.mediaservice.dto.response.InternalMediaResponse;
import com.streaming.mediaservice.dto.response.MediaResponse;
import com.streaming.mediaservice.entity.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface MediaService {

    MediaResponse upload(
            UUID ownerUserId,
            MediaType mediaType,
            MultipartFile file
    );

    void delete(
            UUID ownerUserId,
            UUID mediaId
    );

    InternalMediaResponse getInternalMedia(
            UUID ownerUserId,
            UUID mediaId,
            MediaType expectedType
    );
}