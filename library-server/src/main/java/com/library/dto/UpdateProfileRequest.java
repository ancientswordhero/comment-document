package com.library.dto;

import jakarta.validation.constraints.Size;

public class UpdateProfileRequest {

    @Size(min = 1, max = 50, message = "用户名长度1-50个字符")
    private String username;

    @Size(max = 200, message = "个人留言不能超过200字")
    private String bio;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
}
