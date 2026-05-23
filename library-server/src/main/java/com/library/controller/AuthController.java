package com.library.controller;

import com.library.dto.*;
import com.library.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ApiResponse<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success(userService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(userService.login(request));
    }

    @GetMapping("/me")
    public ApiResponse<LoginResponse> me(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.success(userService.getCurrentUser(userId));
    }

    @PostMapping("/admin")
    public ApiResponse<LoginResponse> createAdmin(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success(userService.createAdmin(request));
    }

    @DeleteMapping("/me")
    public ApiResponse<Void> deleteAccount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        userService.deleteAccount(userId);
        return ApiResponse.success(null);
    }
}
