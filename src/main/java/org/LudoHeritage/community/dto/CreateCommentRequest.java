package org.LudoHeritage.community.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateCommentRequest(
        @NotBlank String authorId,
        @NotBlank String authorName,
        @NotBlank String content
) {
}
