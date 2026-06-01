package org.LudoHeritage.community.dto;

import jakarta.validation.constraints.NotBlank;

public record ToggleLikeRequest(@NotBlank String userId) {
}
