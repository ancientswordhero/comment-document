package com.library.controller;

import com.library.dto.ApiResponse;
import com.library.dto.BookResponse;
import com.library.dto.PageResult;
import com.library.service.BookshelfService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/bookshelf")
public class BookshelfController {

    private final BookshelfService bookshelfService;

    public BookshelfController(BookshelfService bookshelfService) {
        this.bookshelfService = bookshelfService;
    }

    @GetMapping
    public ApiResponse<PageResult<BookResponse>> listBookshelf(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.success(bookshelfService.getBookshelf(userId, page, size));
    }

    @PostMapping("/{bookId}")
    public ApiResponse<Map<String, Object>> addToBookshelf(
            HttpServletRequest request,
            @PathVariable Long bookId) {
        Long userId = (Long) request.getAttribute("userId");
        bookshelfService.addToBookshelf(userId, bookId);
        return ApiResponse.success(Map.of("inBookshelf", true));
    }

    @DeleteMapping("/{bookId}")
    public ApiResponse<Map<String, Object>> removeFromBookshelf(
            HttpServletRequest request,
            @PathVariable Long bookId) {
        Long userId = (Long) request.getAttribute("userId");
        bookshelfService.removeFromBookshelf(userId, bookId);
        return ApiResponse.success(Map.of("inBookshelf", false));
    }

    @GetMapping("/{bookId}")
    public ApiResponse<Map<String, Object>> checkBookshelf(
            HttpServletRequest request,
            @PathVariable Long bookId) {
        Long userId = (Long) request.getAttribute("userId");
        boolean inBookshelf = bookshelfService.isInBookshelf(userId, bookId);
        return ApiResponse.success(Map.of("inBookshelf", inBookshelf));
    }
}
