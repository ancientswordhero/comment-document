package com.library.dto;

import java.time.LocalDateTime;

public class ReportResponse {
    private Long id;
    private Long reviewId;
    private String reviewContent;
    private Long noteId;
    private String noteContent;
    private String targetType;
    private String bookTitle;
    private String reporterName;
    private String reviewAuthorName;
    private String reason;
    private String detail;
    private String status;
    private Long adminId;
    private String adminNote;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getReviewId() { return reviewId; }
    public void setReviewId(Long reviewId) { this.reviewId = reviewId; }
    public String getReviewContent() { return reviewContent; }
    public void setReviewContent(String reviewContent) { this.reviewContent = reviewContent; }
    public Long getNoteId() { return noteId; }
    public void setNoteId(Long noteId) { this.noteId = noteId; }
    public String getNoteContent() { return noteContent; }
    public void setNoteContent(String noteContent) { this.noteContent = noteContent; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }
    public String getReporterName() { return reporterName; }
    public void setReporterName(String reporterName) { this.reporterName = reporterName; }
    public String getReviewAuthorName() { return reviewAuthorName; }
    public void setReviewAuthorName(String reviewAuthorName) { this.reviewAuthorName = reviewAuthorName; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getAdminId() { return adminId; }
    public void setAdminId(Long adminId) { this.adminId = adminId; }
    public String getAdminNote() { return adminNote; }
    public void setAdminNote(String adminNote) { this.adminNote = adminNote; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }

    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private Long id, reviewId, noteId, adminId;
        private String reviewContent, noteContent, targetType;
        private String bookTitle, reporterName, reviewAuthorName;
        private String reason, detail, status, adminNote;
        private LocalDateTime createdAt, resolvedAt;
        public Builder id(Long v) { id = v; return this; }
        public Builder reviewId(Long v) { reviewId = v; return this; }
        public Builder reviewContent(String v) { reviewContent = v; return this; }
        public Builder noteId(Long v) { noteId = v; return this; }
        public Builder noteContent(String v) { noteContent = v; return this; }
        public Builder targetType(String v) { targetType = v; return this; }
        public Builder bookTitle(String v) { bookTitle = v; return this; }
        public Builder reporterName(String v) { reporterName = v; return this; }
        public Builder reviewAuthorName(String v) { reviewAuthorName = v; return this; }
        public Builder reason(String v) { reason = v; return this; }
        public Builder detail(String v) { detail = v; return this; }
        public Builder status(String v) { status = v; return this; }
        public Builder adminId(Long v) { adminId = v; return this; }
        public Builder adminNote(String v) { adminNote = v; return this; }
        public Builder createdAt(LocalDateTime v) { createdAt = v; return this; }
        public Builder resolvedAt(LocalDateTime v) { resolvedAt = v; return this; }
        public ReportResponse build() {
            ReportResponse r = new ReportResponse();
            r.setId(id); r.setReviewId(reviewId); r.setReviewContent(reviewContent);
            r.setNoteId(noteId); r.setNoteContent(noteContent); r.setTargetType(targetType);
            r.setBookTitle(bookTitle); r.setReporterName(reporterName);
            r.setReviewAuthorName(reviewAuthorName); r.setReason(reason);
            r.setDetail(detail); r.setStatus(status); r.setAdminId(adminId);
            r.setAdminNote(adminNote); r.setCreatedAt(createdAt);
            r.setResolvedAt(resolvedAt);
            return r;
        }
    }
}
