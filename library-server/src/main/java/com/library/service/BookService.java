package com.library.service;

import com.library.dto.*;
import com.library.entity.Book;
import com.library.entity.Category;
import com.library.repository.BookRepository;
import com.library.repository.CategoryRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import jakarta.persistence.EntityNotFoundException;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

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

        List<Long> categoryIds = null;
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId).orElse(null);
            if (category != null) {
                if (category.getParentId() == null) {
                    List<Category> children = categoryRepository.findByParentIdOrderBySortOrder(categoryId);
                    categoryIds = children.stream().map(Category::getId).collect(Collectors.toList());
                    categoryIds.add(categoryId);
                } else {
                    categoryIds = List.of(categoryId);
                }
            }
        }

        Page<Book> bookPage = bookRepository.findWithFilters(keyword, categoryIds, 1, pageable);
        return buildPageResult(bookPage);
    }

    public PageResult<BookResponse> getAdminBooks(String keyword, Long categoryId, Integer status, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());

        List<Long> categoryIds = null;
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId).orElse(null);
            if (category != null) {
                if (category.getParentId() == null) {
                    List<Category> children = categoryRepository.findByParentIdOrderBySortOrder(categoryId);
                    categoryIds = children.stream().map(Category::getId).collect(Collectors.toList());
                    categoryIds.add(categoryId);
                } else {
                    categoryIds = List.of(categoryId);
                }
            }
        }

        Page<Book> bookPage = bookRepository.findWithFilters(keyword, categoryIds, status, pageable);
        return buildPageResult(bookPage);
    }

    public BookResponse getBookById(Long id) {
        Book book = bookRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("图书不存在: " + id));
        if (book.getStatus() != null && book.getStatus() == 0) {
            throw new RuntimeException("该图书暂时无法查看：下架中");
        }
        return toResponse(book);
    }

    public BookResponse getBookByIdForAdmin(Long id) {
        Book book = bookRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("图书不存在: " + id));
        return toResponse(book);
    }

    public BookResponse createBook(MultipartFile file, BookRequest req) {
        String title = req.getTitle();
        String author = req.getAuthor();
        byte[] data = null;
        try {
            data = file.getBytes();
            if (title == null || title.isBlank()) {
                title = extractEpubMetadata(data, "dc:title");
            }
            if (author == null || author.isBlank()) {
                author = extractEpubMetadata(data, "dc:creator");
            }
        } catch (Exception e) {
            throw new RuntimeException("EPUB文件读取失败", e);
        }

        Book book = Book.builder()
            .title(title != null ? title : "未知书名")
            .author(author != null ? author : "未知作者")
            .isbn(req.getIsbn())
            .categoryId(req.getCategoryId())
            .coverUrl(req.getCoverUrl())
            .description(req.getDescription())
            .epubData(data)
            .status(1)
            .build();
        book = bookRepository.save(book);
        return toResponse(book);
    }

    public BookResponse updateBook(Long id, MultipartFile file, BookRequest req) {
        Book book = bookRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("图书不存在: " + id));
        book.setTitle(req.getTitle());
        book.setAuthor(req.getAuthor());
        book.setIsbn(req.getIsbn());
        book.setCategoryId(req.getCategoryId());
        book.setCoverUrl(req.getCoverUrl());
        book.setDescription(req.getDescription());
        if (file != null && !file.isEmpty()) {
            try {
                book.setEpubData(file.getBytes());
            } catch (Exception e) {
                throw new RuntimeException("EPUB文件读取失败", e);
            }
        }
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

    public byte[] getEpubData(Long id) {
        Book book = bookRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("图书不存在: " + id));
        if (book.getEpubData() == null) {
            throw new EntityNotFoundException("该图书无EPUB内容");
        }
        return book.getEpubData();
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
            .hasEpub(book.getEpubData() != null)
            .status(book.getStatus())
            .createdAt(book.getCreatedAt())
            .updatedAt(book.getUpdatedAt())
            .build();
    }

    private String extractEpubMetadata(byte[] epubData, String tagName) {
        try (java.util.zip.ZipInputStream zis = new java.util.zip.ZipInputStream(
                new java.io.ByteArrayInputStream(epubData))) {
            java.util.zip.ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().endsWith(".opf")) {
                    StringBuilder sb = new StringBuilder();
                    byte[] buf = new byte[4096];
                    int len;
                    while ((len = zis.read(buf)) > 0) {
                        sb.append(new String(buf, 0, len, java.nio.charset.StandardCharsets.UTF_8));
                    }
                    String xml = sb.toString();
                    int start = xml.indexOf("<" + tagName);
                    if (start >= 0) {
                        start = xml.indexOf(">", start) + 1;
                        int end = xml.indexOf("</" + tagName, start);
                        if (end >= 0) return xml.substring(start, end).trim();
                    }
                    break;
                }
            }
        } catch (Exception ignored) { }
        return null;
    }
}