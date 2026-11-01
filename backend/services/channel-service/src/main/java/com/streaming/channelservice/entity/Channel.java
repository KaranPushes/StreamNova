package com.streaming.channelservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
        name = "channels",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_channels_owner_user_id",
                        columnNames = "owner_user_id"
                ),
                @UniqueConstraint(
                        name = "uk_channels_slug",
                        columnNames = "slug"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Channel extends BaseEntity {

    @Column(name = "owner_user_id", nullable = false, updatable = false)
    private UUID ownerUserId;

    @Column(nullable = false, length = 50)
    private String slug;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(length = 1000)
    private String description;

    @Column(name = "avatar_url", length = 2048)
    private String avatarUrl;

    @Column(name = "banner_url", length = 2048)
    private String bannerUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ChannelStatus status;
}