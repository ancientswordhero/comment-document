package com.library.dto;

import java.time.LocalDateTime;

public class BookResponse {
    private Long id;
    private String title;
    private String author;
    private String isbn;
    private Long categoryId;
    private String categoryName;
    private boolean hasCover;
    private String description;
    private boolean hasEpub;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public BookResponse() {}

    public BookResponse(Long id, String title, String author, String isbn, Long categoryId,
                        String categoryName, boolean hasCover, String description, boolean hasEpub,
                        Integer status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.hasCover = hasCover;
        this.description = description;
        this.hasEpub = hasEpub;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public boolean isHasCover() { return hasCover; }
    public void setHasCover(boolean hasCover) { this.hasCover = hasCover; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isHasEpub() { return hasEpub; }
    public void setHasEpub(boolean hasEpub) { this.hasEpub = hasEpub; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String title;
        private String author;
        private String isbn;
        private Long categoryId;
        private String categoryName;
        private boolean hasCover;
        private String description;
        private boolean hasEpub;
        private Integer status;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder author(String author) { this.author = author; return this; }
        public Builder isbn(String isbn) { this.isbn = isbn; return this; }
        public Builder categoryId(Long categoryId) { this.categoryId = categoryId; return this; }
        public Builder categoryName(String categoryName) { this.categoryName = categoryName; return this; }
        public Builder hasCover(boolean hasCover) { this.hasCover = hasCover; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder hasEpub(boolean hasEpub) { this.hasEpub = hasEpub; return this; }
        public Builder status(Integer status) { this.status = status; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public BookResponse build() {
            return new BookResponse(id, title, author, isbn, categoryId, categoryName,
                hasCover, description, hasEpub, status, createdAt, updatedAt);
        }
    }
}
