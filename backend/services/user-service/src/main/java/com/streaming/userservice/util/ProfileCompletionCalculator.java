package com.streaming.userservice.util;

import com.streaming.userservice.dto.response.ProfileCompletionResponse;
import com.streaming.userservice.entity.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ProfileCompletionCalculator {

    private final List<ProfileField> profileFields = List.of(
            new ProfileField("displayName", User::getDisplayName),
            new ProfileField("bio", User::getBio),
            new ProfileField("avatar", User::getAvatarUrl),
            new ProfileField("banner", User::getBannerUrl),
            new ProfileField("website", User::getWebsiteUrl),
            new ProfileField("location", User::getLocation)
    );

    public ProfileCompletionResponse calculate(User user) {

        List<String> missingFields = new ArrayList<>();

        for (ProfileField field : profileFields) {

            String value = field.extractor().apply(user);

            if (value == null || value.isBlank()) {
                missingFields.add(field.name());
            }
        }

        int percentage =
                ((profileFields.size() - missingFields.size()) * 100)
                        / profileFields.size();

        return ProfileCompletionResponse.builder()
                .percentage(percentage)
                .missingFields(missingFields)
                .build();
    }
}