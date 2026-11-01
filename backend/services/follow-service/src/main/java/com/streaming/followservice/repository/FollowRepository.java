package com.streaming.followservice.repository;

import com.streaming.followservice.entity.Follow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FollowRepository
        extends JpaRepository<Follow, UUID> {

    boolean existsByUserIdAndChannelId(
            UUID userId,
            UUID channelId
    );

    Optional<Follow> findByUserIdAndChannelId(
            UUID userId,
            UUID channelId
    );

    Page<Follow> findByUserId(
            UUID userId,
            Pageable pageable
    );

    List<Follow> findByChannelId(UUID channelId);

    long countByChannelId(UUID channelId);
}