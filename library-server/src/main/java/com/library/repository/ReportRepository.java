package com.library.repository;

import com.library.entity.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long> {

    Page<Report> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);

    Page<Report> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Optional<Report> findByReviewIdAndReporterId(Long reviewId, Long reporterId);

    boolean existsByReviewIdAndReporterId(Long reviewId, Long reporterId);
}
