package com.streaming.streamservice.repository;

import com.streaming.streamservice.entity.Stream;
import com.streaming.streamservice.entity.StreamStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StreamRepository extends JpaRepository<Stream, UUID> {

    List<Stream> findByChannelIdOrderByCreatedAtDesc(
            UUID channelId,
            Pageable pageable
    );

    List<Stream> findByStatusOrderByStartedAtDesc(
            StreamStatus status,
            Pageable pageable
    );

    Optional<Stream> findByIdAndChannelId(
            UUID id,
            UUID channelId
    );

    Optional<Stream> findByStreamKey(String streamKey);

    Page<Stream> findByStatus(
            StreamStatus status,
            Pageable pageable
    );

    Page<Stream> findByStatusAndCategoryIgnoreCase(
            StreamStatus status,
            String category,
            Pageable pageable
    );

    @Query("""
        SELECT s
        FROM Stream s
        WHERE s.status = :status
          AND (
                LOWER(s.title) LIKE LOWER(CONCAT('%', :query, '%'))
             OR LOWER(s.description) LIKE LOWER(CONCAT('%', :query, '%'))
             OR LOWER(s.category) LIKE LOWER(CONCAT('%', :query, '%'))
             OR s.channelId IN :channelIds
          )
    """)
    Page<Stream> searchLiveStreams(
            @Param("status") StreamStatus status,
            @Param("query") String query,
            @Param("channelIds") List<UUID> channelIds,
            Pageable pageable
    );

    @Query("""
        SELECT s
        FROM Stream s
        WHERE s.status = :status
          AND LOWER(s.category) = LOWER(:category)
          AND (
                LOWER(s.title) LIKE LOWER(CONCAT('%', :query, '%'))
             OR LOWER(s.description) LIKE LOWER(CONCAT('%', :query, '%'))
             OR LOWER(s.category) LIKE LOWER(CONCAT('%', :query, '%'))
             OR s.channelId IN :channelIds
          )
    """)
    Page<Stream> searchLiveStreamsByCategory(
            @Param("status") StreamStatus status,
            @Param("category") String category,
            @Param("query") String query,
            @Param("channelIds") List<UUID> channelIds,
            Pageable pageable
    );

    @Query("""
        SELECT s.category, COUNT(s)
        FROM Stream s
        WHERE s.status = :status
          AND s.category IS NOT NULL
          AND TRIM(s.category) <> ''
        GROUP BY s.category
        ORDER BY COUNT(s) DESC
        """)
    List<Object[]> countLiveStreamsByCategory(
            @Param("status") StreamStatus status
    );

    @Query("""
        SELECT s.channelId, COUNT(s)
        FROM Stream s
        WHERE s.status = :status
        GROUP BY s.channelId
        ORDER BY COUNT(s) DESC
    """)
    List<Object[]> countLiveStreamsByChannel(
            @Param("status") StreamStatus status
    );

    @Query("""
        SELECT s
        FROM Stream s
        WHERE s.status = :status
          AND s.channelId IN :channelIds
    """)
    Page<Stream> findLiveStreamsByChannels(
            @Param("status") StreamStatus status,
            @Param("channelIds") List<UUID> channelIds,
            Pageable pageable
    );
}