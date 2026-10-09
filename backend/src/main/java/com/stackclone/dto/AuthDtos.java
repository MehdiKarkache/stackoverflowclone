package com.stackclone.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record RegisterRequest(
            @NotBlank
            @Size(min = 3, max = 40)
            @Pattern(regexp = "^[A-Za-z0-9_.-]+$", message = "lettres, chiffres, '_', '.' et '-' uniquement")
            String username,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8, max = 100) String password) {
    }

    /** login accepte le nom d'utilisateur ou l'email. */
    public record LoginRequest(@NotBlank String login, @NotBlank String password) {
    }

    public record AuthResponse(String token, UserDtos.UserSummary user) {
    }
}
