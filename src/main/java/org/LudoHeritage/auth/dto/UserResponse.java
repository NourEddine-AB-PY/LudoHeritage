package org.LudoHeritage.auth.dto;

import org.LudoHeritage.agent.UserProfile;

public record UserResponse(
        String id,
        String displayName,
        String email,
        boolean onboardingCompleted,
        UserProfile profile
) {
}
