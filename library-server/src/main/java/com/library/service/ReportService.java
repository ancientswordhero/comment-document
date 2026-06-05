package com.library.service;

import com.library.dto.*;
import com.library.entity.Report;
import com.library.entity.Review;
import com.library.entity.User;
import com.library.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final ReviewRepository reviewRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    public ReportService(ReportRepository reportRepository, ReviewRepository reviewRepository,
                         NotificationService notificationService, UserRepository userRepository,
                         BookRepository bookRepository) {
        this.reportRepository = reportRepository;
        this.reviewRepository = reviewRepository;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
    }

    @Transactional
    public void createReport(Long reviewId, Long reporterId, ReportRequest req) {
        if (reportRepository.existsByReviewIdAndReporterId(reviewId, reporterId)) {
            throw new IllegalStateException("您已经举报过这条书评");
        }
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new EntityNotFoundException("书评不存在: " + reviewId));
        if (review.getUserId().equals(reporterId)) {
            throw new IllegalStateException("不能举报自己的书评");
        }
        if ("other".equals(req.getReason()) && (req.getDetail() == null || req.getDetail().trim().isEmpty())) {
            throw new IllegalArgumentException("选择其他时必须填写补充说明");
        }
        Report report = Report.builder()
            .reviewId(reviewId).reporterId(reporterId)
            .reason(req.getReason()).detail(req.getDetail()).status("pending").build();
        reportRepository.save(report);
    }

    public long getPendingCount() {
        return reportRepository.countByStatus("pending");
    }

    public PageResult<ReportResponse> getReports(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<Report> reportPage = (status != null && !status.isEmpty())
            ? reportRepository.findByStatusOrderByCreatedAtDesc(status, pageable)
            : reportRepository.findAllByOrderByCreatedAtDesc(pageable);
        List<ReportResponse> records = reportPage.getContent().stream()
            .map(this::toResponse).collect(Collectors.toList());
        return PageResult.<ReportResponse>builder()
            .records(records).total(reportPage.getTotalElements()).page(page).size(size).build();
    }

    @Transactional
    public void resolveReport(Long reportId, Long adminId, ResolveReportRequest req) {
        Report report = reportRepository.findById(reportId)
            .orElseThrow(() -> new EntityNotFoundException("举报不存在: " + reportId));
        if (!"pending".equals(report.getStatus())) {
            throw new IllegalStateException("该举报已处理");
        }
        Review review = reviewRepository.findById(report.getReviewId())
            .orElseThrow(() -> new EntityNotFoundException("书评不存在"));
        String reviewAuthorName = userRepository.findById(review.getUserId()).map(User::getUsername).orElse("未知用户");
        String bookTitle = bookRepository.findById(review.getBookId()).map(b -> b.getTitle()).orElse("未知图书");
        String reasonLabel = reasonLabel(report.getReason());
        String reviewSnippet = review.getContent().length() > 50 ? review.getContent().substring(0, 50) + "..." : review.getContent();

        if ("delete".equals(req.getAction())) {
            reviewRepository.delete(review);
            report.setStatus("deleted");
            notificationService.createNotification(review.getUserId(), "report_result",
                "你的书评被举报，已被管理员删除",
                "你在《" + bookTitle + "》中的书评「" + reviewSnippet + "」被举报「" + reasonLabel + "」，管理员审核后已删除该评论。",
                review.getBookId(), review.getId());
            notificationService.createNotification(report.getReporterId(), "report_result",
                "你举报的书评已被删除",
                "你在《" + bookTitle + "》中举报的书评「" + reviewSnippet + "」管理员审核后已删除处理。",
                review.getBookId(), review.getId());
        } else if ("dismiss".equals(req.getAction())) {
            report.setStatus("dismissed");
            notificationService.createNotification(review.getUserId(), "report_result",
                "你的书评被举报，举报已被驳回",
                "你在《" + bookTitle + "》中的书评「" + reviewSnippet + "」被举报「" + reasonLabel + "」，管理员审核后驳回了举报。",
                review.getBookId(), review.getId());
            notificationService.createNotification(report.getReporterId(), "report_result",
                "你举报的书评举报被驳回",
                "你在《" + bookTitle + "》中举报的书评「" + reviewSnippet + "」管理员审核后驳回了举报。",
                review.getBookId(), review.getId());
        } else {
            throw new IllegalArgumentException("无效的处理方式: " + req.getAction());
        }
        report.setAdminId(adminId);
        report.setAdminNote(req.getNote());
        report.setResolvedAt(LocalDateTime.now());
        reportRepository.save(report);
    }

    private ReportResponse toResponse(Report report) {
        String reviewContent = "";
        String bookTitle = "";
        Long reviewAuthorId = null;
        try {
            Review review = reviewRepository.findById(report.getReviewId()).orElse(null);
            if (review != null) {
                reviewContent = review.getContent().length() > 100 ? review.getContent().substring(0, 100) + "..." : review.getContent();
                reviewAuthorId = review.getUserId();
                bookTitle = bookRepository.findById(review.getBookId()).map(b -> b.getTitle()).orElse("");
            }
        } catch (Exception ignored) {}
        String reporterName = userRepository.findById(report.getReporterId()).map(User::getUsername).orElse("未知用户");
        String reviewAuthorName = reviewAuthorId != null ? userRepository.findById(reviewAuthorId).map(User::getUsername).orElse("未知用户") : "未知用户";
        return ReportResponse.builder()
            .id(report.getId()).reviewId(report.getReviewId()).reviewContent(reviewContent)
            .bookTitle(bookTitle).reporterName(reporterName).reviewAuthorName(reviewAuthorName)
            .reason(report.getReason()).detail(report.getDetail()).status(report.getStatus())
            .adminId(report.getAdminId()).adminNote(report.getAdminNote())
            .createdAt(report.getCreatedAt()).resolvedAt(report.getResolvedAt()).build();
    }

    private String reasonLabel(String reason) {
        return switch (reason) {
            case "spam" -> "垃圾广告"; case "abuse" -> "人身攻击";
            case "fake" -> "虚假信息"; case "violation" -> "违规内容"; case "other" -> "其他";
            default -> reason;
        };
    }
}
