package com.library.service;

import com.library.dto.*;
import com.library.entity.Book;
import com.library.entity.Category;
import com.library.repository.BookRepository;
import com.library.repository.CategoryRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityNotFoundException;

import java.util.List;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;

    public BookService(BookRepository bookRepository, CategoryRepository categoryRepository) {
        this.bookRepository = bookRepository;
        this.categoryRepository = categoryRepository;
    }

    public PageResult<BookResponse> getBooks(String keyword, Long categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<Book> bookPage = bookRepository.findWithFilters(keyword, categoryId, 1, pageable);
        return buildPageResult(bookPage);
    }

    public PageResult<BookResponse> getAdminBooks(String keyword, Long categoryId, Integer status, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<Book> bookPage = bookRepository.findWithFilters(keyword, categoryId, status, pageable);
        return buildPageResult(bookPage);
    }

    public BookResponse getBookById(Long id) {
        Book book = bookRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("图书不存在: " + id));
        return toResponse(book);
    }

    public BookResponse createBook(BookRequest req) {
        Book book = Book.builder()
            .title(req.getTitle())
            .author(req.getAuthor())
            .isbn(req.getIsbn())
            .categoryId(req.getCategoryId())
            .coverUrl(req.getCoverUrl())
            .description(req.getDescription())
            .status(1)
            .build();
        book = bookRepository.save(book);
        return toResponse(book);
    }

    public BookResponse updateBook(Long id, BookRequest req) {
        Book book = bookRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("图书不存在: " + id));
        book.setTitle(req.getTitle());
        book.setAuthor(req.getAuthor());
        book.setIsbn(req.getIsbn());
        book.setCategoryId(req.getCategoryId());
        book.setCoverUrl(req.getCoverUrl());
        book.setDescription(req.getDescription());
        book = bookRepository.save(book);
        return toResponse(book);
    }

    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new EntityNotFoundException("图书不存在: " + id);
        }
        bookRepository.deleteById(id);
    }

    public BookResponse toggleStatus(Long id) {
        Book book = bookRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("图书不存在: " + id));
        book.setStatus(book.getStatus() == 1 ? 0 : 1);
        book = bookRepository.save(book);
        return toResponse(book);
    }

    private PageResult<BookResponse> buildPageResult(Page<Book> page) {
        List<BookResponse> records = page.getContent().stream()
            .map(this::toResponse)
            .toList();
        return PageResult.<BookResponse>builder()
            .records(records)
            .total(page.getTotalElements())
            .page(page.getNumber() + 1)
            .size(page.getSize())
            .build();
    }

    private BookResponse toResponse(Book book) {
        String categoryName = null;
        if (book.getCategoryId() != null) {
            categoryName = categoryRepository.findById(book.getCategoryId())
                .map(Category::getName)
                .orElse(null);
        }
        return BookResponse.builder()
            .id(book.getId())
            .title(book.getTitle())
            .author(book.getAuthor())
            .isbn(book.getIsbn())
            .categoryId(book.getCategoryId())
            .categoryName(categoryName)
            .coverUrl(book.getCoverUrl())
            .description(book.getDescription())
            .status(book.getStatus())
            .createdAt(book.getCreatedAt())
            .updatedAt(book.getUpdatedAt())
            .build();
    }
}
