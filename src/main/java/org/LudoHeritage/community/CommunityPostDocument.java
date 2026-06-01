package org.LudoHeritage.community;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "community_posts")
public class CommunityPostDocument {
    @Id
    private String id;
    private String authorId;
    private String authorName;
    private String authorEmail;
    private String content;
    private String linkedGameId;
    private String linkedGameName;
    private String customGameName;
    private String imageDataUrl;
    private Instant createdAt;
    private List<String> likedUserIds = new ArrayList<>();
    private List<CommunityComment> comments = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getLinkedGameId() {
        return linkedGameId;
    }

    public void setLinkedGameId(String linkedGameId) {
        this.linkedGameId = linkedGameId;
    }

    public String getLinkedGameName() {
        return linkedGameName;
    }

    public void setLinkedGameName(String linkedGameName) {
        this.linkedGameName = linkedGameName;
    }

    public String getCustomGameName() {
        return customGameName;
    }

    public void setCustomGameName(String customGameName) {
        this.customGameName = customGameName;
    }

    public String getImageDataUrl() {
        return imageDataUrl;
    }

    public void setImageDataUrl(String imageDataUrl) {
        this.imageDataUrl = imageDataUrl;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public List<String> getLikedUserIds() {
        return likedUserIds;
    }

    public void setLikedUserIds(List<String> likedUserIds) {
        this.likedUserIds = likedUserIds;
    }

    public List<CommunityComment> getComments() {
        return comments;
    }

    public void setComments(List<CommunityComment> comments) {
        this.comments = comments;
    }
}
