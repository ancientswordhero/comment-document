package com.library.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 100)
    private String author;

    @Column(nullable = false, unique = true, length = 20)
    private String isbn;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "cover_url", length = 500)
    private String coverUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Lob
    @Column(columnDefinition = "MEDIUMBLOB")
    private byte[] coverData;

    @Lob
    @Column(columnDefinition = "MEDIUMBLOB")
    private byte[] epubData;

    @Column(nullable = false)
    private Integer status = 1;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Book() {}

    public Book(Long id, String title, String author, String isbn, Long categoryId,
                String coverUrl, String description, byte[] epubData, byte[] coverData,
                Integer status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.categoryId = categoryId;
        this.coverUrl = coverUrl;
        this.description = description;
        this.epubData = epubData;
        this.coverData = coverData;
        this.status = status != null ? status : 1;
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
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public byte[] getCoverData() { return coverData; }
    public void setCoverData(byte[] coverData) { this.coverData = coverData; }
    public byte[] getEpubData() { return epubData; }
    public void setEpubData(byte[] epubData) { this.epubData = epubData; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String title;
        private String author;
        private String isbn;
        private Long categoryId;
        private String coverUrl;
        private String description;
        private byte[] epubData;
        private byte[] coverData;
        private Integer status = 1;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder author(String author) { this.author = author; return this; }
        public Builder isbn(String isbn) { this.isbn = isbn; return this; }
        public Builder categoryId(Long categoryId) { this.categoryId = categoryId; return this; }
        public Builder coverUrl(String coverUrl) { this.coverUrl = coverUrl; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder epubData(byte[] epubData) { this.epubData = epubData; return this; }
        public Builder coverData(byte[] coverData) { this.coverData = coverData; return this; }
        public Builder status(Integer status) { this.status = status; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public Book build() {
            return new Book(id, title, author, isbn, categoryId, coverUrl, description,
                epubData, coverData, status, createdAt, updatedAt);
        }
    }
}
