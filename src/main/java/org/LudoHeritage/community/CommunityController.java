package org.LudoHeritage.community;

import jakarta.validation.Valid;
import org.LudoHeritage.community.dto.CommunityPostResponse;
import org.LudoHeritage.community.dto.CreateCommentRequest;
import org.LudoHeritage.community.dto.CreatePostRequest;
import org.LudoHeritage.community.dto.ToggleLikeRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/community")
public class CommunityController {
    private final CommunityService communityService;

    public CommunityController(CommunityService communityService) {
        this.communityService = communityService;
    }

    @GetMapping("/posts")
    public ResponseEntity<List<CommunityPostResponse>> listPosts() {
        return ResponseEntity.ok(communityService.listPosts());
    }

    @PostMapping("/posts")
    public ResponseEntity<CommunityPostResponse> createPost(@Valid @RequestBody CreatePostRequest request) {
        return ResponseEntity.ok(communityService.createPost(request));
    }

    @PostMapping("/posts/{postId}/likes")
    public ResponseEntity<CommunityPostResponse> toggleLike(
            @PathVariable String postId,
            @Valid @RequestBody ToggleLikeRequest request
    ) {
        return ResponseEntity.ok(communityService.toggleLike(postId, request.userId()));
    }

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<CommunityPostResponse> addComment(
            @PathVariable String postId,
            @Valid @RequestBody CreateCommentRequest request
    ) {
        return ResponseEntity.ok(communityService.addComment(postId, request));
    }

    @DeleteMapping("/posts")
    public ResponseEntity<Void> deleteAllPosts() {
        communityService.deleteAllPosts();
        return ResponseEntity.noContent().build();
    }
}
