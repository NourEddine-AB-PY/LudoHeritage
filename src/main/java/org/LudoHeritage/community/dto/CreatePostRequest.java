package org.LudoHeritage.community.dto;

import jakarta.validation.constraints.NotBlank;

public record CreatePostRequest(
        @NotBlank String authorId,
        @NotBlank String authorName,
        String authorEmail,
        @NotBlank String content,
        String linkedGameId,
        String linkedGameName,
        String customGameName,
        String imageDataUrl
) {
}
