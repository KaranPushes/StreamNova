package com.streaming.mediaservice.repository;

import com.streaming.mediaservice.entity.Media;
import com.streaming.mediaservice.entity.MediaStatus;
import com.streaming.mediaservice.entity.MediaType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MediaRepository extends JpaRepository<Media, UUID> {

    List<Media> findByOwnerUserIdAndMediaTypeAndStatus(
            UUID ownerUserId,
            MediaType mediaType,
            MediaStatus status
    );

    Optional<Media> findByIdAndOwnerUserIdAndStatus(
            UUID id,
            UUID ownerUserId,
            MediaStatus status
    );
}