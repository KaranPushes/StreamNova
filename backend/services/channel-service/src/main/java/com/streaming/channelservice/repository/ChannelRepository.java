package com.streaming.channelservice.repository;

import com.streaming.channelservice.entity.Channel;
import com.streaming.channelservice.entity.ChannelStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface ChannelRepository extends JpaRepository<Channel, UUID> {

    boolean existsByOwnerUserId(UUID ownerUserId);

    boolean existsBySlugIgnoreCase(String slug);

    Optional<Channel> findByOwnerUserId(UUID ownerUserId);

    Optional<Channel> findBySlugIgnoreCase(String slug);

    @Query("""
    SELECT c
    FROM Channel c
    WHERE c.status = :status
      AND (
          LOWER(c.slug) LIKE LOWER(CONCAT('%', :query, '%'))
          OR LOWER(c.displayName) LIKE LOWER(CONCAT('%', :query, '%'))
      )
    ORDER BY c.displayName ASC
    """)
    Page<Channel> searchChannels(
            String query,
            ChannelStatus status,
            Pageable pageable
    );

    Page<Channel> findByStatus(
            ChannelStatus status,
            Pageable pageable
    );
}