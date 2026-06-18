package com.library.service;

import com.library.dto.*;
import com.library.entity.Note;
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
    private final NoteRepository noteRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    public ReportService(ReportRepository reportRepository, ReviewRepository reviewRepository,
                         NoteRepository noteRepository,
                         NotificationService notificationService, UserRepository userRepository,
                         BookRepository bookRepository) {
        this.reportRepository = reportRepository;
        this.reviewRepository = reviewRepository;
        this.noteRepository = noteRepository;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
    }

    @Transactional
    public void createReport(ReportRequest req, Long reporterId) {
        if ("note".equals(req.getTargetType())) {
            createNoteReport(req, reporterId);
        } else {
            createReviewReport(req, reporterId);
        }
    }

    @Transactional
    public void createReport(Long reviewId, Long reporterId, ReportRequest req) {
        req.setReviewId(reviewId);
        createReport(req, reporterId);
    }

    private void createReviewReport(ReportRequest req, Long reporterId) {
        if (reportRepository.existsByReviewIdAndReporterId(req.getReviewId(), reporterId)) {
            throw new IllegalStateException("您已经举报过这条书评");
        }
        Review review = reviewRepository.findById(req.getReviewId())
            .orElseThrow(() -> new EntityNotFoundException("书评不存在: " + req.getReviewId()));
        if (review.getUserId().equals(reporterId)) {
            throw new IllegalStateException("不能举报自己的书评");
        }
        if ("other".equals(req.getReason()) && (req.getDetail() == null || req.getDetail().trim().isEmpty())) {
            throw new IllegalArgumentException("选择其他时必须填写补充说明");
        }
        Report report = Report.builder()
            .reviewId(req.getReviewId()).reporterId(reporterId)
            .reason(req.getReason()).detail(req.getDetail())
            .targetType("review").status("pending").build();
        try {
            reportRepository.save(report);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new IllegalStateException("您已经举报过这条书评");
        }
    }

    private void createNoteReport(ReportRequest req, Long reporterId) {
        if (req.getNoteId() == null) {
            throw new IllegalArgumentException("手记ID不能为空");
        }
        if (reporterId == null) {
            throw new IllegalArgumentException("请先登录后再举报");
        }
        if (reportRepository.existsByNoteIdAndReporterId(req.getNoteId(), reporterId)) {
            throw new IllegalStateException("您已经举报过这条手记");
        }
        Note note = noteRepository.findById(req.getNoteId())
            .orElseThrow(() -> new EntityNotFoundException("手记不存在: " + req.getNoteId()));
        if (note.getUserId().equals(reporterId)) {
            throw new IllegalStateException("不能举报自己的手记");
        }
        if ("other".equals(req.getReason()) && (req.getDetail() == null || req.getDetail().trim().isEmpty())) {
            throw new IllegalArgumentException("选择其他时必须填写补充说明");
        }
        Report report = Report.builder()
            .noteId(req.getNoteId()).reporterId(reporterId)
            .reason(req.getReason()).detail(req.getDetail())
            .targetType("note").status("pending").build();
        try {
            reportRepository.save(report);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new IllegalStateException("您已经举报过这条手记");
        }
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

        if ("note".equals(report.getTargetType())) {
            resolveNoteReport(report, adminId, req);
        } else {
            resolveReviewReport(report, adminId, req);
        }

        report.setAdminId(adminId);
        report.setAdminNote(req.getNote());
        report.setResolvedAt(LocalDateTime.now());
        reportRepository.save(report);
    }

    private void resolveReviewReport(Report report, Long adminId, ResolveReportRequest req) {
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
    }

    private void resolveNoteReport(Report report, Long adminId, ResolveReportRequest req) {
        Note note = noteRepository.findById(report.getNoteId())
            .orElseThrow(() -> new EntityNotFoundException("手记不存在"));
        String noteAuthorName = userRepository.findById(note.getUserId()).map(User::getUsername).orElse("未知用户");
        String bookTitle = bookRepository.findById(note.getBookId()).map(b -> b.getTitle()).orElse("未知图书");
        String reasonLabel = reasonLabel(report.getReason());
        String noteSnippet = note.getContent().length() > 50 ? note.getContent().substring(0, 50) + "..." : note.getContent();

        if ("delete".equals(req.getAction())) {
            noteRepository.delete(note);
            report.setStatus("deleted");
            notificationService.createNoteNotification(note.getUserId(), "report_result",
                "你的手记被举报，已被管理员删除",
                "你在《" + bookTitle + "》中的手记「" + noteSnippet + "」被举报「" + reasonLabel + "」，管理员审核后已删除该手记。",
                note.getBookId(), note.getId());
            notificationService.createNoteNotification(report.getReporterId(), "report_result",
                "你举报的手记已被删除",
                "你在《" + bookTitle + "》中举报的手记「" + noteSnippet + "」管理员审核后已删除处理。",
                note.getBookId(), note.getId());
        } else if ("dismiss".equals(req.getAction())) {
            report.setStatus("dismissed");
            notificationService.createNoteNotification(note.getUserId(), "report_result",
                "你的手记被举报，举报已被驳回",
                "你在《" + bookTitle + "》中的手记「" + noteSnippet + "」被举报「" + reasonLabel + "」，管理员审核后驳回了举报。",
                note.getBookId(), note.getId());
            notificationService.createNoteNotification(report.getReporterId(), "report_result",
                "你举报的手记举报被驳回",
                "你在《" + bookTitle + "》中举报的手记「" + noteSnippet + "」管理员审核后驳回了举报。",
                note.getBookId(), note.getId());
        } else {
            throw new IllegalArgumentException("无效的处理方式: " + req.getAction());
        }
    }

    private ReportResponse toResponse(Report report) {
        if ("note".equals(report.getTargetType())) {
            return toNoteResponse(report);
        } else {
            return toReviewResponse(report);
        }
    }

    private ReportResponse toReviewResponse(Report report) {
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
            .targetType("review")
            .adminId(report.getAdminId()).adminNote(report.getAdminNote())
            .createdAt(report.getCreatedAt()).resolvedAt(report.getResolvedAt()).build();
    }

    private ReportResponse toNoteResponse(Report report) {
        String noteContent = "";
        String bookTitle = "";
        Long noteAuthorId = null;
        try {
            Note note = noteRepository.findById(report.getNoteId()).orElse(null);
            if (note != null) {
                noteContent = note.getContent().length() > 100 ? note.getContent().substring(0, 100) + "..." : note.getContent();
                noteAuthorId = note.getUserId();
                bookTitle = bookRepository.findById(note.getBookId()).map(b -> b.getTitle()).orElse("");
            }
        } catch (Exception ignored) {}
        String reporterName = userRepository.findById(report.getReporterId()).map(User::getUsername).orElse("未知用户");
        String noteAuthorName = noteAuthorId != null ? userRepository.findById(noteAuthorId).map(User::getUsername).orElse("未知用户") : "未知用户";
        return ReportResponse.builder()
            .id(report.getId()).noteId(report.getNoteId()).noteContent(noteContent)
            .bookTitle(bookTitle).reporterName(reporterName).reviewAuthorName(noteAuthorName)
            .reason(report.getReason()).detail(report.getDetail()).status(report.getStatus())
            .targetType("note")
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
