package com.library.repository;

import com.library.entity.UserBook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserBookRepository extends JpaRepository<UserBook, Long> {
    Page<UserBook> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Optional<UserBook> findByUserIdAndBookId(Long userId, Long bookId);
    boolean existsByUserIdAndBookId(Long userId, Long bookId);
    void deleteByUserIdAndBookId(Long userId, Long bookId);
}
