package org.LudoHeritage.community.dto;

import java.time.Instant;
import java.util.List;

public record CommunityPostResponse(
        String id,
        String authorId,
        String authorName,
        String authorEmail,
        String content,
        String linkedGameId,
        String linkedGameName,
        String customGameName,
        String imageDataUrl,
        Instant createdAt,
        int likeCount,
        List<String> likedUserIds,
        List<CommunityCommentResponse> comments
) {
}
