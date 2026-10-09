package com.stackclone.dto;

import com.stackclone.model.User;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public final class UserDtos {

    private UserDtos() {
    }

    public record UserSummary(Long id, String username, int reputation, String role) {
        public static UserSummary from(User u) {
            return new UserSummary(u.getId(), u.getUsername(), u.getReputation(), u.getRole().name());
        }
    }

    public record UserProfile(Long id, String username, String bio, int reputation, String role,
                              Instant createdAt, long questionCount, long answerCount) {
    }

    public record UpdateProfileRequest(@Size(max = 2000) String bio) {
    }
}
