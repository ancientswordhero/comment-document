package com.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ReportRequest {

    private Long reviewId;

    private Long noteId;

    private String targetType = "review";

    @NotBlank(message = "举报理由不能为空")
    private String reason;

    @Size(max = 200, message = "补充说明不能超过200字")
    private String detail;

    public Long getReviewId() { return reviewId; }
    public void setReviewId(Long reviewId) { this.reviewId = reviewId; }
    public Long getNoteId() { return noteId; }
    public void setNoteId(Long noteId) { this.noteId = noteId; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
}
