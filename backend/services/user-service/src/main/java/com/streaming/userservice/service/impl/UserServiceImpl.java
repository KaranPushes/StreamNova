package com.streaming.userservice.service.impl;

import com.streaming.userservice.client.MediaServiceClient;
import com.streaming.userservice.constants.Constants;
import com.streaming.userservice.dto.client.InternalMediaResponse;
import com.streaming.userservice.dto.request.*;
import com.streaming.userservice.dto.response.*;
import com.streaming.userservice.entity.User;
import com.streaming.userservice.entity.UserStatus;
import com.streaming.userservice.exception.*;
import com.streaming.userservice.mapper.UserMapper;
import com.streaming.userservice.repository.UserRepository;
import com.streaming.userservice.service.UserService;
import com.streaming.userservice.util.ProfileCompletionCalculator;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final ProfileCompletionCalculator profileCompletionCalculator;
    private final MediaServiceClient mediaServiceClient;

    private static final Logger log =
            LoggerFactory.getLogger(UserServiceImpl.class);

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {

        String normalizedEmail =
                request.email().trim().toLowerCase(Locale.ROOT);

        String normalizedUsername =
                request.username().trim().toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new EmailAlreadyExistsException(normalizedEmail);
        }

        if (userRepository.existsByUsernameIgnoreCase(normalizedUsername)) {
            throw new UsernameAlreadyExistsException(normalizedUsername);
        }

        User user = userMapper.toEntity(request);

        user.setId(UUID.randomUUID());
        user.setEmail(normalizedEmail);
        user.setUsername(normalizedUsername);

        User savedUser = userRepository.save(user);

        log.info("Created user with username={}", normalizedUsername);

        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public PublicUserResponse getCurrentUser(UUID id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        return userMapper.toPublicResponse(user);
    }

    public UserResponse getUserById(UUID id) {

        User user = userRepository.findByIdAndStatus(id, UserStatus.ACTIVE)
                .orElseThrow(() ->
                        new ResourceNotFoundException(Constants.USER, "id", id));

        return userMapper.toResponse(user);
    }

    @Override
    public PublicUserResponse getPublicProfile(String username) {

        User user = userRepository.findByUsernameAndStatus(username, UserStatus.ACTIVE)
                .orElseThrow(() ->
                        new ResourceNotFoundException(Constants.USER, "username", username));

        return userMapper.toPublicResponse(user);
    }
    @Override
    public PublicUserResponse getPublicProfileById(UUID id) {

        User user = userRepository.findByIdAndStatus(id, UserStatus.ACTIVE)
                .orElseThrow(() ->
                        new ResourceNotFoundException(Constants.USER, "id", id));

        return userMapper.toPublicResponse(user);
    }
    @Override
    public UserResponse getInternalUserById(UUID id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(Constants.USER, "id", id));

        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse getUserByEmail(String email) {

        User user = userRepository.findByEmailAndStatus(email, UserStatus.ACTIVE)
                .orElseThrow(() ->
                        new ResourceNotFoundException(Constants.USER, "email", email));

        return userMapper.toResponse(user);
    }


    @Override
    @Transactional
    public PublicUserResponse updateUser(UUID id, UpdateUserRequest request) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(Constants.USER, "id", id));

        if (request.username() != null) {

            String normalizedUsername =
                    request.username().trim().toLowerCase(Locale.ROOT);

            if (!user.getUsername().equalsIgnoreCase(normalizedUsername)
                    && userRepository.existsByUsernameIgnoreCase(normalizedUsername)) {
                throw new UsernameAlreadyExistsException(normalizedUsername);
            }

            user.setUsername(normalizedUsername);
        }

        if (request.displayName() != null) {
            user.setDisplayName(request.displayName());
        }

        if (request.bio() != null) {
            user.setBio(request.bio());
        }

        if (request.websiteUrl() != null) {
            user.setWebsiteUrl(request.websiteUrl());
        }

        if (request.location() != null) {
            user.setLocation(request.location());
        }

        User updatedUser = userRepository.save(user);

        log.info("Updated profile for user {}", id);

        return userMapper.toPublicResponse(updatedUser);
    }

    @Override
    public UsernameAvailabilityResponse checkUsernameAvailability(String username) {

        String normalizedUsername =
                username.trim().toLowerCase(Locale.ROOT);

        boolean available =
                !userRepository.existsByUsernameIgnoreCase(normalizedUsername);

        return UsernameAvailabilityResponse.builder()
                .username(normalizedUsername)
                .available(available)
                .build();
    }

    @Override
    @Transactional
    public void deactivateUser(UUID id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User", "id", id));

        if (user.getStatus() == UserStatus.DEACTIVATED) {
            throw new AlreadyInStateException("User", "id", id, "DEACTIVATED");
        }

        user.setStatus(UserStatus.DEACTIVATED);

        userRepository.save(user);
    }

    @Override
    @Transactional
    public PublicUserResponse updateAvatar(
            UUID id,
            UpdateAvatarRequest request) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                Constants.USER,
                                "id",
                                id
                        ));

        InternalMediaResponse media;

        try {
            media = mediaServiceClient.getMedia(
                    request.mediaId(),
                    "AVATAR",
                    id
            );

        } catch (FeignException.NotFound ex) {

            throw new ResourceNotFoundException(
                    "Media",
                    "id",
                    request.mediaId()
            );

        } catch (FeignException.BadRequest ex) {

            throw new IllegalArgumentException(
                    "Selected media is not a valid avatar."
            );
        }

        user.setAvatarMediaId(media.id());
        user.setAvatarUrl(media.url());

        log.info(
                "Updated avatar for user={} mediaId={}",
                id,
                media.id()
        );

        return userMapper.toPublicResponse(
                userRepository.save(user)
        );
    }

    @Override
    @Transactional
    public PublicUserResponse updateBanner(
            UUID id,
            UpdateBannerRequest request) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                Constants.USER,
                                "id",
                                id
                        ));

        InternalMediaResponse media;

        try {
            media = mediaServiceClient.getMedia(
                    request.mediaId(),
                    "BANNER",
                    id
            );

        } catch (FeignException.NotFound ex) {

            throw new ResourceNotFoundException(
                    "Media",
                    "id",
                    request.mediaId()
            );

        } catch (FeignException.BadRequest ex) {

            throw new IllegalArgumentException(
                    "Selected media is not a valid banner."
            );
        }

        user.setBannerMediaId(media.id());
        user.setBannerUrl(media.url());

        log.info(
                "Updated banner for user={} mediaId={}",
                id,
                media.id()
        );

        return userMapper.toPublicResponse(
                userRepository.save(user)
        );
    }

    @Override
    public ProfileCompletionResponse getProfileCompletion(UUID id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(Constants.USER, "id", id));

        return profileCompletionCalculator.calculate(user);
    }

    @Override
    public List<UserSearchResponse> searchUsers(String query) {

        Pageable limit = PageRequest.of(0, 20);

        return userRepository.searchUsers(
                        query.trim(),
                        UserStatus.ACTIVE,
                        limit
                )
                .stream()
                .map(userMapper::toSearchResponse)
                .toList();
    }

    @Override
    @Transactional
    public UserResponse updateUserStatus(
            UUID id,
            UpdateUserStatusRequest request
    ) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        user.setStatus(request.status());

        User updatedUser = userRepository.save(user);

        log.info(
                "Updated user status id={} status={}",
                updatedUser.getId(),
                updatedUser.getStatus()
        );

        return userMapper.toResponse(updatedUser);
    }

    @Override
    @Transactional
    public void deleteAvatar(UUID id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException(id));

        user.setAvatarMediaId(null);
        user.setAvatarUrl(null);

        log.info("Deleted avatar reference for user id={}", id);
    }

    @Override
    @Transactional
    public void deleteBanner(UUID id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException(id));

        user.setBannerMediaId(null);
        user.setBannerUrl(null);

        log.info("Deleted banner reference for user id={}", id);
    }
}