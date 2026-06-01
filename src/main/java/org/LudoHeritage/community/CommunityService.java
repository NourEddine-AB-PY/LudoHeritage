package org.LudoHeritage.community;

import org.LudoHeritage.community.dto.CommunityCommentResponse;
import org.LudoHeritage.community.dto.CommunityPostResponse;
import org.LudoHeritage.community.dto.CreateCommentRequest;
import org.LudoHeritage.community.dto.CreatePostRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class CommunityService {
    private final CommunityPostRepository communityPostRepository;

    public CommunityService(CommunityPostRepository communityPostRepository) {
        this.communityPostRepository = communityPostRepository;
    }

    public List<CommunityPostResponse> listPosts() {
        return communityPostRepository.findAll().stream()
                .sorted(Comparator.comparing(CommunityPostDocument::getCreatedAt).reversed())
                .map(this::toResponse)
                .toList();
    }

    public CommunityPostResponse createPost(CreatePostRequest request) {
        CommunityPostDocument post = new CommunityPostDocument();
        post.setAuthorId(request.authorId().trim());
        post.setAuthorName(request.authorName().trim());
        post.setAuthorEmail(blankToNull(request.authorEmail()));
        post.setContent(request.content().trim());
        post.setLinkedGameId(blankToNull(request.linkedGameId()));
        post.setLinkedGameName(blankToNull(request.linkedGameName()));
        post.setCustomGameName(blankToNull(request.customGameName()));
        post.setImageDataUrl(blankToNull(request.imageDataUrl()));
        post.setCreatedAt(Instant.now());
        return toResponse(communityPostRepository.save(post));
    }

    public CommunityPostResponse toggleLike(String postId, String userId) {
        CommunityPostDocument post = getPost(postId);
        if (post.getLikedUserIds().contains(userId)) {
            post.getLikedUserIds().remove(userId);
        } else {
            post.getLikedUserIds().add(userId);
        }
        return toResponse(communityPostRepository.save(post));
    }

    public CommunityPostResponse addComment(String postId, CreateCommentRequest request) {
        CommunityPostDocument post = getPost(postId);

        CommunityComment comment = new CommunityComment();
        comment.setId(UUID.randomUUID().toString());
        comment.setAuthorId(request.authorId().trim());
        comment.setAuthorName(request.authorName().trim());
        comment.setContent(request.content().trim());
        comment.setCreatedAt(Instant.now());

        post.getComments().add(comment);
        return toResponse(communityPostRepository.save(post));
    }

    public void deleteAllPosts() {
        communityPostRepository.deleteAll();
    }

    private CommunityPostDocument getPost(String postId) {
        return communityPostRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found."));
    }

    private CommunityPostResponse toResponse(CommunityPostDocument post) {
        return new CommunityPostResponse(
                post.getId(),
                post.getAuthorId(),
                post.getAuthorName(),
                post.getAuthorEmail(),
                post.getContent(),
                post.getLinkedGameId(),
                post.getLinkedGameName(),
                post.getCustomGameName(),
                post.getImageDataUrl(),
                post.getCreatedAt(),
                post.getLikedUserIds().size(),
                post.getLikedUserIds(),
                post.getComments().stream()
                        .sorted(Comparator.comparing(CommunityComment::getCreatedAt))
                        .map(comment -> new CommunityCommentResponse(
                                comment.getId(),
                                comment.getAuthorId(),
                                comment.getAuthorName(),
                                comment.getContent(),
                                comment.getCreatedAt()
                        ))
                        .toList()
        );
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
