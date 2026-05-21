package com.library.dto;

import jakarta.validation.constraints.NotBlank;

public class ResolveReportRequest {

    @NotBlank(message = "处理方式不能为空")
    private String action;  // "delete" or "dismiss"

    private String note;

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
