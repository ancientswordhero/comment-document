package com.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ReviewRequest {

    @NotBlank(message = "书评内容不能为空")
    @Size(max = 1000, message = "书评内容不能超过1000字")
    private String content;

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
