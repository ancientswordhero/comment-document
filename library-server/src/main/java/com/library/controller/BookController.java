package com.library.controller;

import com.library.dto.*;
import com.library.service.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class BookController {

    private final BookService bookService;
    private final CategoryService categoryService;

    public BookController(BookService bookService, CategoryService categoryService) {
        this.bookService = bookService;
        this.categoryService = categoryService;
    }

    @GetMapping("/books")
    public ApiResponse<PageResult<BookResponse>> listBooks(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(bookService.getBooks(keyword, categoryId, page, size));
    }

    @GetMapping("/books/{id}")
    public ApiResponse<BookResponse> getBook(@PathVariable Long id) {
        return ApiResponse.success(bookService.getBookById(id));
    }

    @GetMapping("/categories")
    public ApiResponse<java.util.List<CategoryResponse>> getCategories() {
        return ApiResponse.success(categoryService.getCategoryTree());
    }
}
