package com.library.controller;

import com.library.dto.*;
import com.library.service.ReviewService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/api/books/{bookId}/reviews")
    public ApiResponse<PageResult<ReviewResponse>> listReviews(
            @PathVariable Long bookId,
            @RequestParam(defaultValue = "time") String sort,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.success(reviewService.getReviews(bookId, sort, page, size, userId));
    }

    @PostMapping("/api/books/{bookId}/reviews")
    public ApiResponse<ReviewResponse> createReview(
            @PathVariable Long bookId,
            @Valid @RequestBody ReviewRequest reviewRequest,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.success(reviewService.createReview(bookId, userId, reviewRequest));
    }

    @PostMapping("/api/reviews/{id}/reply")
    public ApiResponse<ReviewResponse> createReply(
            @PathVariable Long id,
            @Valid @RequestBody ReviewRequest reviewRequest,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.success(reviewService.createReply(id, userId, reviewRequest));
    }

    @PutMapping("/api/reviews/{id}")
    public ApiResponse<ReviewResponse> updateReview(
            @PathVariable Long id,
            @Valid @RequestBody ReviewRequest reviewRequest,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        boolean isAdmin = "ADMIN".equals(request.getAttribute("role"));
        return ApiResponse.success(reviewService.updateReview(id, userId, isAdmin, reviewRequest));
    }

    @DeleteMapping("/api/reviews/{id}")
    public ApiResponse<Void> deleteReview(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        boolean isAdmin = "ADMIN".equals(request.getAttribute("role"));
        reviewService.deleteReview(id, userId, isAdmin);
        return ApiResponse.success(null);
    }

    @PostMapping("/api/reviews/{id}/like")
    public ApiResponse<Boolean> toggleLike(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.success(reviewService.toggleLike(id, userId));
    }
}
