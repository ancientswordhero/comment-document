package com.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class NoteRequest {

    @NotBlank(message = "内容不能为空")
    private String content;

    @Size(max = 500, message = "引用原文不能超过500字")
    private String selectedText;

    @Size(max = 500, message = "CFI不能超过500字")
    private String cfi;

    @NotBlank(message = "类型不能为空")
    private String type;

    private boolean publish;

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getSelectedText() { return selectedText; }
    public void setSelectedText(String selectedText) { this.selectedText = selectedText; }
    public String getCfi() { return cfi; }
    public void setCfi(String cfi) { this.cfi = cfi; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public boolean isPublish() { return publish; }
    public void setPublish(boolean publish) { this.publish = publish; }
}
