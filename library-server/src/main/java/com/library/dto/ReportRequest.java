package com.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ReportRequest {

    @NotBlank(message = "举报理由不能为空")
    private String reason;

    @Size(max = 200, message = "补充说明不能超过200字")
    private String detail;

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
}
