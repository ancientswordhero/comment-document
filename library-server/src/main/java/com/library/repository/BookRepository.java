package com.library.repository;

import com.library.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {

    @Query("SELECT b FROM Book b WHERE " +
           "(:keyword IS NULL OR b.title LIKE CONCAT('%', :keyword, '%') OR b.author LIKE CONCAT('%', :keyword, '%') OR b.isbn LIKE CONCAT('%', :keyword, '%')) AND " +
           "(:categoryIds IS NULL OR b.categoryId IN :categoryIds) AND " +
           "(:status IS NULL OR b.status = :status)")
    Page<Book> findWithFilters(@Param("keyword") String keyword,
                                @Param("categoryIds") List<Long> categoryIds,
                                @Param("status") Integer status,
                                Pageable pageable);
}