package com.library.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ReviewResponse {
    private Long id;
    private Long bookId;
    private Long userId;
    private String username;
    private Long parentId;
    private Long rootId;
    private String content;
    private int likeCount;
    private int replyCount;
    private boolean liked;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ReviewResponse> replies;

    public ReviewResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public Long getRootId() { return rootId; }
    public void setRootId(Long rootId) { this.rootId = rootId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }
    public int getReplyCount() { return replyCount; }
    public void setReplyCount(int replyCount) { this.replyCount = replyCount; }
    public boolean isLiked() { return liked; }
    public void setLiked(boolean liked) { this.liked = liked; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public List<ReviewResponse> getReplies() { return replies; }
    public void setReplies(List<ReviewResponse> replies) { this.replies = replies; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private Long bookId;
        private Long userId;
        private String username;
        private Long parentId;
        private Long rootId;
        private String content;
        private int likeCount;
        private int replyCount;
        private boolean liked;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private List<ReviewResponse> replies;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder bookId(Long bookId) { this.bookId = bookId; return this; }
        public Builder userId(Long userId) { this.userId = userId; return this; }
        public Builder username(String username) { this.username = username; return this; }
        public Builder parentId(Long parentId) { this.parentId = parentId; return this; }
        public Builder rootId(Long rootId) { this.rootId = rootId; return this; }
        public Builder content(String content) { this.content = content; return this; }
        public Builder likeCount(int likeCount) { this.likeCount = likeCount; return this; }
        public Builder replyCount(int replyCount) { this.replyCount = replyCount; return this; }
        public Builder liked(boolean liked) { this.liked = liked; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public Builder replies(List<ReviewResponse> replies) { this.replies = replies; return this; }
        public ReviewResponse build() {
            ReviewResponse r = new ReviewResponse();
            r.setId(id); r.setBookId(bookId); r.setUserId(userId);
            r.setUsername(username); r.setParentId(parentId); r.setRootId(rootId);
            r.setContent(content); r.setLikeCount(likeCount); r.setReplyCount(replyCount);
            r.setLiked(liked); r.setCreatedAt(createdAt); r.setUpdatedAt(updatedAt);
            r.setReplies(replies);
            return r;
        }
    }
}
