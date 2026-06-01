package org.LudoHeritage.auth.dto;

public record AuthResponse(
        String message,
        boolean requiresOnboarding,
        UserResponse user
) {
}
