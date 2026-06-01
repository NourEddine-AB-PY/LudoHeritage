package org.LudoHeritage.community.dto;

import java.time.Instant;

public record CommunityCommentResponse(
        String id,
        String authorId,
        String authorName,
        String content,
        Instant createdAt
) {
}
