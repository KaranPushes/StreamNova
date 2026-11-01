package com.streaming.userservice.service;

import com.streaming.userservice.dto.request.*;
import com.streaming.userservice.dto.response.*;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserResponse createUser(CreateUserRequest request);

    PublicUserResponse getCurrentUser(UUID id);

    UserResponse getUserByEmail(String email);

    PublicUserResponse getPublicProfile(String username);

    PublicUserResponse getPublicProfileById(UUID id);

    PublicUserResponse updateUser(UUID id, UpdateUserRequest request);

    void deactivateUser(UUID id);

    UserResponse getInternalUserById(UUID id);

    PublicUserResponse updateAvatar(UUID id, UpdateAvatarRequest request);

    PublicUserResponse updateBanner(UUID id, UpdateBannerRequest request);

    UsernameAvailabilityResponse checkUsernameAvailability(String username);

    ProfileCompletionResponse getProfileCompletion(UUID id);

    List<UserSearchResponse> searchUsers(String query);

    UserResponse updateUserStatus(
            UUID id,
            UpdateUserStatusRequest request
    );

    void deleteAvatar(UUID id);

    void deleteBanner(UUID id);
}