package com.library.controller;

import com.library.dto.*;
import com.library.service.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@RestController
@RequestMapping("/api/admin")
public class AdminBookController {

    private final BookService bookService;
    private final FileService fileService;

    public AdminBookController(BookService bookService, FileService fileService) {
        this.bookService = bookService;
        this.fileService = fileService;
    }

    @GetMapping("/books")
    public ApiResponse<PageResult<BookResponse>> listBooks(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(bookService.getAdminBooks(keyword, categoryId, status, page, size));
    }

    @PostMapping("/books")
    public ApiResponse<BookResponse> createBook(@Valid @RequestBody BookRequest request) {
        return ApiResponse.success(bookService.createBook(request));
    }

    @GetMapping("/books/{id}")
    public ApiResponse<BookResponse> getBookById(@PathVariable Long id) {
        return ApiResponse.success(bookService.getBookByIdForAdmin(id));
    }

    @PutMapping("/books/{id}")
    public ApiResponse<BookResponse> updateBook(@PathVariable Long id, @Valid @RequestBody BookRequest request) {
        return ApiResponse.success(bookService.updateBook(id, request));
    }

    @DeleteMapping("/books/{id}")
    public ApiResponse<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ApiResponse.success(null);
    }

    @PutMapping("/books/{id}/status")
    public ApiResponse<BookResponse> toggleStatus(@PathVariable Long id) {
        return ApiResponse.success(bookService.toggleStatus(id));
    }

    @PostMapping("/upload/cover")
    public ApiResponse<String> uploadCover(@RequestParam("file") MultipartFile file) throws IOException {
        String path = fileService.store(file);
        return ApiResponse.success(path);
    }
}
