package org.LudoHeritage.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record OnboardingRequest(
        @NotBlank String niveau,
        @NotBlank String typeJeuPrefere,
        @NotBlank String regionPreferee,
        @NotBlank String langue
) {
}
