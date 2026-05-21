package com.library.controller;

import com.library.dto.*;
import com.library.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ApiResponse<PageResult<NotificationResponse>> listNotifications(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.success(notificationService.getNotifications(userId, page, size));
    }

    @GetMapping("/unread-count")
    public ApiResponse<Integer> getUnreadCount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.success(notificationService.getUnreadCount(userId));
    }

    @PutMapping("/{id}/read")
    public ApiResponse<Void> markAsRead(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        notificationService.markAsRead(id, userId);
        return ApiResponse.success(null);
    }

    @PutMapping("/read-all")
    public ApiResponse<Void> markAllAsRead(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        notificationService.markAllAsRead(userId);
        return ApiResponse.success(null);
    }
}
