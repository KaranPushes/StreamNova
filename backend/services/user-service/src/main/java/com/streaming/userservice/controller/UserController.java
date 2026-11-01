package com.streaming.userservice.controller;

import com.streaming.userservice.dto.request.UpdateAvatarRequest;
import com.streaming.userservice.dto.request.UpdateBannerRequest;
import com.streaming.userservice.dto.request.UpdateUserRequest;
import com.streaming.userservice.dto.response.ProfileCompletionResponse;
import com.streaming.userservice.dto.response.PublicUserResponse;
import com.streaming.userservice.dto.response.UserSearchResponse;
import com.streaming.userservice.dto.response.UsernameAvailabilityResponse;
import com.streaming.userservice.security.CurrentUserProvider;
import com.streaming.userservice.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping("/me")
    public PublicUserResponse getCurrentUser(
            @AuthenticationPrincipal Jwt jwt) {

        UUID userId = currentUserProvider.getUserId(jwt);

        return userService.getCurrentUser(userId);
    }

    @GetMapping("/profile/{username}")
    public PublicUserResponse getPublicProfile(
            @PathVariable String username) {

        return userService.getPublicProfile(username);
    }

    @GetMapping("/id/{id}")
    public PublicUserResponse getPublicProfileById(
            @PathVariable UUID id) {

        return userService.getPublicProfileById(id);
    }

    @PatchMapping("/me/profile")
    public PublicUserResponse updateProfile(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateUserRequest request) {

        UUID userId = currentUserProvider.getUserId(jwt);

        return userService.updateUser(userId, request);
    }

    @PatchMapping("/me/avatar")
    public PublicUserResponse updateAvatar(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateAvatarRequest request) {

        UUID userId = currentUserProvider.getUserId(jwt);

        return userService.updateAvatar(userId, request);
    }

    @PatchMapping("/me/banner")
    public PublicUserResponse updateBanner(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateBannerRequest request) {

        UUID userId = currentUserProvider.getUserId(jwt);

        return userService.updateBanner(userId, request);
    }

    @GetMapping("/availability")
    public UsernameAvailabilityResponse checkUsernameAvailability(
            @RequestParam String username) {

        return userService.checkUsernameAvailability(username);
    }

    @GetMapping("/me/profile-completion")
    public ProfileCompletionResponse getProfileCompletion(
            @AuthenticationPrincipal Jwt jwt) {

        UUID userId = currentUserProvider.getUserId(jwt);

        return userService.getProfileCompletion(userId);
    }

    @GetMapping("/search")
    public List<UserSearchResponse> searchUsers(
            @RequestParam("q")
            @Size(min = 2, max = 50)
            String query) {

        return userService.searchUsers(query);
    }

    @DeleteMapping("/me/avatar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAvatar(
            @AuthenticationPrincipal Jwt jwt) {

        UUID userId = currentUserProvider.getUserId(jwt);

        userService.deleteAvatar(userId);
    }

    @DeleteMapping("/me/banner")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBanner(
            @AuthenticationPrincipal Jwt jwt) {

        UUID userId = currentUserProvider.getUserId(jwt);

        userService.deleteBanner(userId);
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivateCurrentUser(
            @AuthenticationPrincipal Jwt jwt) {

        UUID userId = currentUserProvider.getUserId(jwt);

        userService.deactivateUser(userId);
    }

}