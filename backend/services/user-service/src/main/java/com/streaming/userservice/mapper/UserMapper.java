package com.streaming.userservice.mapper;

import com.streaming.userservice.dto.request.CreateUserRequest;
import com.streaming.userservice.dto.response.PublicUserResponse;
import com.streaming.userservice.dto.response.UserResponse;
import com.streaming.userservice.dto.response.UserSearchResponse;
import com.streaming.userservice.entity.User;
import com.streaming.userservice.entity.UserStatus;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(CreateUserRequest request) {

        return User.builder()
                .username(request.username())
                .displayName(request.displayName())
                .email(request.email())
                .status(UserStatus.ACTIVE)
                .build();
    }

    public UserResponse toResponse(User user) {

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .email(user.getEmail())

                .bio(user.getBio())

                .avatarMediaId(user.getAvatarMediaId())
                .avatarUrl(user.getAvatarUrl())

                .bannerMediaId(user.getBannerMediaId())
                .bannerUrl(user.getBannerUrl())

                .websiteUrl(user.getWebsiteUrl())
                .location(user.getLocation())

                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public PublicUserResponse toPublicResponse(User user) {

        return PublicUserResponse.builder()
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .bio(user.getBio())
                .avatarUrl(user.getAvatarUrl())
                .bannerUrl(user.getBannerUrl())
                .websiteUrl(user.getWebsiteUrl())
                .location(user.getLocation())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public UserSearchResponse toSearchResponse(User user) {

        return UserSearchResponse.builder()
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }
}