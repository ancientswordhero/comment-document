package com.library.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notes")
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "book_id", nullable = false)
    private Long bookId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "selected_text", length = 500)
    private String selectedText;

    @Column(length = 500)
    private String cfi;

    @Column(nullable = false, length = 20)
    private String type;

    @Column(name = "is_published")
    private boolean published;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "root_id")
    private Long rootId;

    @Column(name = "like_count")
    private int likeCount;

    @Column(name = "reply_count")
    private int replyCount;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Note() {}

    public Note(Long id, Long userId, Long bookId, String content, String selectedText,
                String cfi, String type, boolean published, Long parentId, Long rootId,
                int likeCount, int replyCount, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.bookId = bookId;
        this.content = content;
        this.selectedText = selectedText;
        this.cfi = cfi;
        this.type = type;
        this.published = published;
        this.parentId = parentId;
        this.rootId = rootId;
        this.likeCount = likeCount;
        this.replyCount = replyCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }
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
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id, userId, bookId, parentId, rootId;
        private String content, selectedText, cfi, type;
        private boolean published;
        private int likeCount, replyCount;
        private LocalDateTime createdAt, updatedAt;

        public Builder id(Long v) { id = v; return this; }
        public Builder userId(Long v) { userId = v; return this; }
        public Builder bookId(Long v) { bookId = v; return this; }
        public Builder content(String v) { content = v; return this; }
        public Builder selectedText(String v) { selectedText = v; return this; }
        public Builder cfi(String v) { cfi = v; return this; }
        public Builder type(String v) { type = v; return this; }
        public Builder published(boolean v) { published = v; return this; }
        public Builder parentId(Long v) { parentId = v; return this; }
        public Builder rootId(Long v) { rootId = v; return this; }
        public Builder likeCount(int v) { likeCount = v; return this; }
        public Builder replyCount(int v) { replyCount = v; return this; }
        public Builder createdAt(LocalDateTime v) { createdAt = v; return this; }
        public Builder updatedAt(LocalDateTime v) { updatedAt = v; return this; }
        public Note build() {
            return new Note(id, userId, bookId, content, selectedText, cfi, type,
                published, parentId, rootId, likeCount, replyCount, createdAt, updatedAt);
        }
    }
}
