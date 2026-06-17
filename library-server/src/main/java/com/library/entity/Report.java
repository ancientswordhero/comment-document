package com.library.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reports", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"review_id", "reporter_id"}),
    @UniqueConstraint(columnNames = {"note_id", "reporter_id"})
})
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "review_id")
    private Long reviewId;

    @Column(name = "note_id")
    private Long noteId;

    @Column(nullable = false, length = 20)
    private String targetType = "review";

    @Column(name = "reporter_id", nullable = false)
    private Long reporterId;

    @Column(nullable = false, length = 20)
    private String reason;

    @Column(length = 200)
    private String detail;

    @Column(nullable = false, length = 20)
    private String status = "pending";

    @Column(name = "admin_id")
    private Long adminId;

    @Column(name = "admin_note", length = 200)
    private String adminNote;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    public Report() {}

    @PrePersist protected void onCreate() { createdAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getReviewId() { return reviewId; }
    public void setReviewId(Long reviewId) { this.reviewId = reviewId; }
    public Long getNoteId() { return noteId; }
    public void setNoteId(Long noteId) { this.noteId = noteId; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public Long getReporterId() { return reporterId; }
    public void setReporterId(Long reporterId) { this.reporterId = reporterId; }
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
        private Long id, reviewId, noteId, reporterId, adminId;
        private String reason, detail, status = "pending", targetType = "review", adminNote;
        private LocalDateTime createdAt, resolvedAt;
        public Builder id(Long v) { id = v; return this; }
        public Builder reviewId(Long v) { reviewId = v; return this; }
        public Builder noteId(Long v) { noteId = v; return this; }
        public Builder reporterId(Long v) { reporterId = v; return this; }
        public Builder reason(String v) { reason = v; return this; }
        public Builder detail(String v) { detail = v; return this; }
        public Builder status(String v) { status = v; return this; }
        public Builder targetType(String v) { targetType = v; return this; }
        public Builder adminId(Long v) { adminId = v; return this; }
        public Builder adminNote(String v) { adminNote = v; return this; }
        public Builder createdAt(LocalDateTime v) { createdAt = v; return this; }
        public Builder resolvedAt(LocalDateTime v) { resolvedAt = v; return this; }
        public Report build() {
            Report r = new Report();
            r.setId(id); r.setReviewId(reviewId); r.setNoteId(noteId); r.setReporterId(reporterId);
            r.setReason(reason); r.setDetail(detail);
            r.setStatus(status != null ? status : "pending");
            r.setTargetType(targetType != null ? targetType : "review");
            r.setAdminId(adminId); r.setAdminNote(adminNote);
            r.setCreatedAt(createdAt); r.setResolvedAt(resolvedAt);
            return r;
        }
    }
}
