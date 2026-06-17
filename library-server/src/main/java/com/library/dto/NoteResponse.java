package com.library.dto;

import java.time.LocalDateTime;
import java.util.List;

public class NoteResponse {
    private Long id;
    private Long userId;
    private String username;
    private Long bookId;
    private String bookTitle;
    private String content;
    private String selectedText;
    private String cfi;
    private String type;
    private boolean published;
    private Long parentId;
    private Long rootId;
    private int likeCount;
    private int replyCount;
    private boolean liked;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<NoteResponse> replies;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }
    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getSelectedText() { return selectedText; }
    public void setSelectedText(String selectedText) { this.selectedText = selectedText; }
    public String getCfi() { return cfi; }
    public void setCfi(String cfi) { this.cfi = cfi; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public boolean isPublished() { return published; }
    public void setPublished(boolean published) { this.published = published; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public Long getRootId() { return rootId; }
    public void setRootId(Long rootId) { this.rootId = rootId; }
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
    public List<NoteResponse> getReplies() { return replies; }
    public void setReplies(List<NoteResponse> replies) { this.replies = replies; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id, userId, bookId, parentId, rootId;
        private String username, bookTitle, content, selectedText, cfi, type;
        private boolean published, liked;
        private int likeCount, replyCount;
        private LocalDateTime createdAt, updatedAt;
        private List<NoteResponse> replies;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder userId(Long userId) { this.userId = userId; return this; }
        public Builder username(String username) { this.username = username; return this; }
        public Builder bookId(Long bookId) { this.bookId = bookId; return this; }
        public Builder bookTitle(String bookTitle) { this.bookTitle = bookTitle; return this; }
        public Builder content(String content) { this.content = content; return this; }
        public Builder selectedText(String selectedText) { this.selectedText = selectedText; return this; }
        public Builder cfi(String cfi) { this.cfi = cfi; return this; }
        public Builder type(String type) { this.type = type; return this; }
        public Builder published(boolean published) { this.published = published; return this; }
        public Builder parentId(Long parentId) { this.parentId = parentId; return this; }
        public Builder rootId(Long rootId) { this.rootId = rootId; return this; }
        public Builder likeCount(int likeCount) { this.likeCount = likeCount; return this; }
        public Builder replyCount(int replyCount) { this.replyCount = replyCount; return this; }
        public Builder liked(boolean liked) { this.liked = liked; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public Builder replies(List<NoteResponse> replies) { this.replies = replies; return this; }
        public NoteResponse build() {
            NoteResponse r = new NoteResponse();
            r.setId(id); r.setUserId(userId); r.setUsername(username);
            r.setBookId(bookId); r.setBookTitle(bookTitle);
            r.setContent(content); r.setSelectedText(selectedText); r.setCfi(cfi);
            r.setType(type); r.setPublished(published);
            r.setParentId(parentId); r.setRootId(rootId);
            r.setLikeCount(likeCount); r.setReplyCount(replyCount);
            r.setLiked(liked); r.setCreatedAt(createdAt); r.setUpdatedAt(updatedAt);
            r.setReplies(replies);
            return r;
        }
    }
}
