package com.library.controller;

import com.library.dto.*;
import com.library.service.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    public ApiResponse<BookResponse> createBook(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "author", required = false) String author,
            @RequestParam("isbn") String isbn,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "description", required = false) String description) {
        BookRequest req = new BookRequest();
        req.setTitle(title);
        req.setAuthor(author);
        req.setIsbn(isbn);
        if (categoryId != null) req.setCategoryId(categoryId);
        if (description != null) req.setDescription(description);
        return ApiResponse.success(bookService.createBook(file, null, req));
    }

    @GetMapping("/books/{id}")
    public ApiResponse<BookResponse> getBookById(@PathVariable Long id) {
        return ApiResponse.success(bookService.getBookByIdForAdmin(id));
    }

    @PutMapping("/books/{id}")
    public ApiResponse<BookResponse> updateBook(
            @PathVariable Long id,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("author") String author,
            @RequestParam("isbn") String isbn,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "description", required = false) String description) {
        BookRequest req = new BookRequest();
        req.setTitle(title);
        req.setAuthor(author);
        req.setIsbn(isbn);
        if (categoryId != null) req.setCategoryId(categoryId);
        if (description != null) req.setDescription(description);
        return ApiResponse.success(bookService.updateBook(id, file, null, req));
    }

    @GetMapping("/books/{id}/epub")
    public ResponseEntity<byte[]> getEpub(@PathVariable Long id) {
        byte[] data = bookService.getEpubData(id);
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("application/epub+zip"))
                .body(data);
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
