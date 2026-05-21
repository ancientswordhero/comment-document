package com.library.service;

import com.library.dto.BookResponse;
import com.library.dto.PageResult;
import com.library.entity.Book;
import com.library.entity.Category;
import com.library.entity.UserBook;
import com.library.repository.BookRepository;
import com.library.repository.CategoryRepository;
import com.library.repository.UserBookRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BookshelfService {

    private final UserBookRepository userBookRepository;
    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;

    public BookshelfService(UserBookRepository userBookRepository, BookRepository bookRepository,
                            CategoryRepository categoryRepository) {
        this.userBookRepository = userBookRepository;
        this.bookRepository = bookRepository;
        this.categoryRepository = categoryRepository;
    }

    public PageResult<BookResponse> getBookshelf(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<UserBook> userBooks = userBookRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        List<Long> bookIds = userBooks.getContent().stream()
                .map(UserBook::getBookId).collect(Collectors.toList());
        Map<Long, Book> bookMap = bookRepository.findAllById(bookIds).stream()
                .collect(Collectors.toMap(Book::getId, b -> b));

        List<BookResponse> records = userBooks.getContent().stream()
                .map(ub -> {
                    Book book = bookMap.get(ub.getBookId());
                    return book != null ? toResponse(book) : null;
                })
                .filter(r -> r != null)
                .collect(Collectors.toList());

        return new PageResult<>(records, userBooks.getTotalElements(), page, size);
    }

    private BookResponse toResponse(Book book) {
        String categoryName = null;
        if (book.getCategoryId() != null) {
            categoryName = categoryRepository.findById(book.getCategoryId())
                    .map(Category::getName).orElse(null);
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
                .content(book.getContent())
                .status(book.getStatus())
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .build();
    }

    public void addToBookshelf(Long userId, Long bookId) {
        if (!userBookRepository.existsByUserIdAndBookId(userId, bookId)) {
            userBookRepository.save(new UserBook(userId, bookId));
        }
    }

    @Transactional
    public void removeFromBookshelf(Long userId, Long bookId) {
        userBookRepository.deleteByUserIdAndBookId(userId, bookId);
    }

    public boolean isInBookshelf(Long userId, Long bookId) {
        return userBookRepository.existsByUserIdAndBookId(userId, bookId);
    }
}
