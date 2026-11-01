package com.streaming.streamservice.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "streams",
        indexes = {
                @Index(
                        name = "idx_streams_channel_id",
                        columnList = "channel_id"
                ),
                @Index(
                        name = "idx_streams_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_streams_scheduled_at",
                        columnList = "scheduled_at"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stream extends BaseEntity {

    @Column(name = "channel_id", nullable = false, updatable = false)
    private UUID channelId;

    @Column(name = "stream_key", length = 128, unique = true)
    private String streamKey;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(length = 100)
    private String category;

    @Column(name = "thumbnail_url", length = 2048)
    private String thumbnailUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StreamStatus status;

    @Column(name = "scheduled_at")
    private Instant scheduledAt;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "ended_at")
    private Instant endedAt;
}