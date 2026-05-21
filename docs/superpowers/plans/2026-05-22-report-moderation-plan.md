# 举报与审核系统 — 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现书评举报、管理审核、站内通知系统：用户举报不当书评→管理员审核删除/驳回→举报人和被举报人收通知。

**Architecture:** Spring Boot 新增 Report/Notification 实体 + 3 个 Controller（读者端举报+通知，管理端审核）。前端读者端新增 ReportDialog 弹窗 + Inbox 收件箱页面 + BannerHeader 红点通知。管理端新增 ReportManagement 页面 + 导航链接。

**Tech Stack:** Java 17, Spring Boot 3.x, Spring Data JPA, MySQL, JUnit + Mockito, MockMvc, Vue 3, Axios

---

### Task 1: 创建 Report 和 Notification 实体

**Files:**
- Create: `library-server/src/main/java/com/library/entity/Report.java`
- Create: `library-server/src/main/java/com/library/entity/Notification.java`

- [ ] **Step 1: 创建 Report 实体**

```java
package com.library.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reports", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"review_id", "reporter_id"})
})
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "review_id", nullable = false)
    private Long reviewId;

    @Column(name = "reporter_id", nullable = false)
    private Long reporterId;

    @Column(nullable = false, length = 20)
    private String reason;

    @Column(length = 200)
    private String detail;

    @Column(nullable = false, length = 20)
    private String status = "pending";

    @Column(name = "admin_id")
    private Long adminId;

    @Column(name = "admin_note", length = 200)
    private String adminNote;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    public Report() {}

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getReviewId() { return reviewId; }
    public void setReviewId(Long reviewId) { this.reviewId = reviewId; }
    public Long getReporterId() { return reporterId; }
    public void setReporterId(Long reporterId) { this.reporterId = reporterId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getAdminId() { return adminId; }
    public void setAdminId(Long adminId) { this.adminId = adminId; }
    public String getAdminNote() { return adminNote; }
    public void setAdminNote(String adminNote) { this.adminNote = adminNote; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id, reviewId, reporterId, adminId;
        private String reason, detail, status = "pending", adminNote;
        private LocalDateTime createdAt, resolvedAt;

        public Builder id(Long v) { id = v; return this; }
        public Builder reviewId(Long v) { reviewId = v; return this; }
        public Builder reporterId(Long v) { reporterId = v; return this; }
        public Builder reason(String v) { reason = v; return this; }
        public Builder detail(String v) { detail = v; return this; }
        public Builder status(String v) { status = v; return this; }
        public Builder adminId(Long v) { adminId = v; return this; }
        public Builder adminNote(String v) { adminNote = v; return this; }
        public Builder createdAt(LocalDateTime v) { createdAt = v; return this; }
        public Builder resolvedAt(LocalDateTime v) { resolvedAt = v; return this; }

        public Report build() {
            Report r = new Report();
            r.setId(id); r.setReviewId(reviewId); r.setReporterId(reporterId);
            r.setReason(reason); r.setDetail(detail); r.setStatus(status != null ? status : "pending");
            r.setAdminId(adminId); r.setAdminNote(adminNote);
            r.setCreatedAt(createdAt); r.setResolvedAt(resolvedAt);
            return r;
        }
    }
}
```

- [ ] **Step 2: 创建 Notification 实体**

```java
package com.library.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 20)
    private String type;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_read")
    private int isRead;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public Notification() {}

    public Notification(Long userId, String type, String title, String content) {
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.content = content;
        this.isRead = 0;
    }

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public int getIsRead() { return isRead; }
    public void setIsRead(int isRead) { this.isRead = isRead; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
```

- [ ] **Step 3: 验证编译**

Run: `cd library-server && ./mvnw compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add library-server/src/main/java/com/library/entity/Report.java library-server/src/main/java/com/library/entity/Notification.java
git commit -m "feat: add Report and Notification JPA entities"
```

---

### Task 2: 创建 Repository 层

**Files:**
- Create: `library-server/src/main/java/com/library/repository/ReportRepository.java`
- Create: `library-server/src/main/java/com/library/repository/NotificationRepository.java`

- [ ] **Step 1: 创建 ReportRepository**

```java
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
```

- [ ] **Step 2: 创建 NotificationRepository**

```java
package com.library.repository;

import com.library.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.isRead = 0")
    int countUnread(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = 1 WHERE n.userId = :userId AND n.isRead = 0")
    void markAllAsRead(@Param("userId") Long userId);
}
```

- [ ] **Step 3: 验证编译**

Run: `cd library-server && ./mvnw compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add library-server/src/main/java/com/library/repository/ReportRepository.java library-server/src/main/java/com/library/repository/NotificationRepository.java
git commit -m "feat: add ReportRepository and NotificationRepository"
```

---

### Task 3: 创建 DTO 类

**Files:**
- Create: `library-server/src/main/java/com/library/dto/ReportRequest.java`
- Create: `library-server/src/main/java/com/library/dto/ReportResponse.java`
- Create: `library-server/src/main/java/com/library/dto/ResolveReportRequest.java`
- Create: `library-server/src/main/java/com/library/dto/NotificationResponse.java`

- [ ] **Step 1: 创建 ReportRequest**

```java
package com.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ReportRequest {

    @NotBlank(message = "举报理由不能为空")
    private String reason;

    @Size(max = 200, message = "补充说明不能超过200字")
    private String detail;

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
}
```

- [ ] **Step 2: 创建 ReportResponse**

```java
package com.library.dto;

import java.time.LocalDateTime;

public class ReportResponse {
    private Long id;
    private Long reviewId;
    private String reviewContent;
    private String bookTitle;
    private String reporterName;
    private String reviewAuthorName;
    private String reason;
    private String detail;
    private String status;
    private Long adminId;
    private String adminNote;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getReviewId() { return reviewId; }
    public void setReviewId(Long reviewId) { this.reviewId = reviewId; }
    public String getReviewContent() { return reviewContent; }
    public void setReviewContent(String reviewContent) { this.reviewContent = reviewContent; }
    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }
    public String getReporterName() { return reporterName; }
    public void setReporterName(String reporterName) { this.reporterName = reporterName; }
    public String getReviewAuthorName() { return reviewAuthorName; }
    public void setReviewAuthorName(String reviewAuthorName) { this.reviewAuthorName = reviewAuthorName; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getAdminId() { return adminId; }
    public void setAdminId(Long adminId) { this.adminId = adminId; }
    public String getAdminNote() { return adminNote; }
    public void setAdminNote(String adminNote) { this.adminNote = adminNote; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id, reviewId, adminId;
        private String reviewContent, bookTitle, reporterName, reviewAuthorName;
        private String reason, detail, status, adminNote;
        private LocalDateTime createdAt, resolvedAt;

        public Builder id(Long v) { id = v; return this; }
        public Builder reviewId(Long v) { reviewId = v; return this; }
        public Builder reviewContent(String v) { reviewContent = v; return this; }
        public Builder bookTitle(String v) { bookTitle = v; return this; }
        public Builder reporterName(String v) { reporterName = v; return this; }
        public Builder reviewAuthorName(String v) { reviewAuthorName = v; return this; }
        public Builder reason(String v) { reason = v; return this; }
        public Builder detail(String v) { detail = v; return this; }
        public Builder status(String v) { status = v; return this; }
        public Builder adminId(Long v) { adminId = v; return this; }
        public Builder adminNote(String v) { adminNote = v; return this; }
        public Builder createdAt(LocalDateTime v) { createdAt = v; return this; }
        public Builder resolvedAt(LocalDateTime v) { resolvedAt = v; return this; }
        public ReportResponse build() {
            ReportResponse r = new ReportResponse();
            r.setId(id); r.setReviewId(reviewId); r.setReviewContent(reviewContent);
            r.setBookTitle(bookTitle); r.setReporterName(reporterName);
            r.setReviewAuthorName(reviewAuthorName);
            r.setReason(reason); r.setDetail(detail); r.setStatus(status);
            r.setAdminId(adminId); r.setAdminNote(adminNote);
            r.setCreatedAt(createdAt); r.setResolvedAt(resolvedAt);
            return r;
        }
    }
}
```

- [ ] **Step 3: 创建 ResolveReportRequest**

```java
package com.library.dto;

import jakarta.validation.constraints.NotBlank;

public class ResolveReportRequest {

    @NotBlank(message = "处理方式不能为空")
    private String action;  // "delete" or "dismiss"

    private String note;

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
```

- [ ] **Step 4: 创建 NotificationResponse**

```java
package com.library.dto;

import java.time.LocalDateTime;

public class NotificationResponse {
    private Long id;
    private String type;
    private String title;
    private String content;
    private boolean isRead;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
```

- [ ] **Step 5: 验证编译**

Run: `cd library-server && ./mvnw compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 6: Commit**

```bash
git add library-server/src/main/java/com/library/dto/ReportRequest.java library-server/src/main/java/com/library/dto/ReportResponse.java library-server/src/main/java/com/library/dto/ResolveReportRequest.java library-server/src/main/java/com/library/dto/NotificationResponse.java
git commit -m "feat: add report and notification DTOs"
```

---

### Task 4: 创建 ReportService + 测试（TDD）

**Files:**
- Create: `library-server/src/test/java/com/library/service/ReportServiceTest.java`
- Create: `library-server/src/main/java/com/library/service/ReportService.java`

- [ ] **Step 1: 编写 ReportServiceTest（测试先行）**

```java
package com.library.service;

import com.library.dto.*;
import com.library.entity.Report;
import com.library.entity.Review;
import com.library.entity.User;
import com.library.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock ReportRepository reportRepository;
    @Mock ReviewRepository reviewRepository;
    @Mock UserRepository userRepository;
    @Mock NotificationService notificationService;
    @InjectMocks ReportService reportService;

    @Test
    void shouldCreateReport() {
        ReportRequest req = new ReportRequest();
        req.setReason("spam");
        when(reportRepository.existsByReviewIdAndReporterId(1L, 5L)).thenReturn(false);
        Review review = Review.builder().id(1L).userId(10L).content("被举报内容").build();
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reportRepository.save(any(Report.class))).thenAnswer(inv -> {
            Report r = inv.getArgument(0);
            r.setId(100L);
            return r;
        });

        reportService.createReport(1L, 5L, req);

        verify(reportRepository).save(any(Report.class));
    }

    @Test
    void shouldRejectDuplicateReport() {
        when(reportRepository.existsByReviewIdAndReporterId(1L, 5L)).thenReturn(true);

        ReportRequest req = new ReportRequest();
        req.setReason("spam");

        assertThatThrownBy(() -> reportService.createReport(1L, 5L, req))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("已经举报过");
    }

    @Test
    void shouldRejectSelfReport() {
        when(reportRepository.existsByReviewIdAndReporterId(1L, 5L)).thenReturn(false);
        Review review = Review.builder().id(1L).userId(5L).content("自己的评论").build();
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        ReportRequest req = new ReportRequest();
        req.setReason("spam");

        assertThatThrownBy(() -> reportService.createReport(1L, 5L, req))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("不能举报自己的书评");
    }

    @Test
    void shouldResolveWithDelete() {
        Report report = Report.builder().id(1L).reviewId(10L).reporterId(3L)
            .status("pending").build();
        when(reportRepository.findById(1L)).thenReturn(Optional.of(report));
        Review review = Review.builder().id(10L).userId(7L).content("违规内容").build();
        when(reviewRepository.findById(10L)).thenReturn(Optional.of(review));
        when(userRepository.findById(3L)).thenReturn(Optional.of(
            new User(3L, "举报人", "pw", "READER", null)));
        when(userRepository.findById(7L)).thenReturn(Optional.of(
            new User(7L, "被举报人", "pw", "READER", null)));

        ResolveReportRequest req = new ResolveReportRequest();
        req.setAction("delete");

        reportService.resolveReport(1L, 2L, req);

        verify(reviewRepository).delete(review);
        verify(reportRepository).save(argThat(r -> "deleted".equals(r.getStatus())));
        verify(notificationService, times(2)).createNotification(
            anyLong(), anyString(), anyString(), anyString());
    }

    @Test
    void shouldResolveWithDismiss() {
        Report report = Report.builder().id(1L).reviewId(10L).reporterId(3L)
            .status("pending").reason("spam").build();
        when(reportRepository.findById(1L)).thenReturn(Optional.of(report));
        Review review = Review.builder().id(10L).userId(7L).content("正常内容").build();
        when(reviewRepository.findById(10L)).thenReturn(Optional.of(review));
        when(userRepository.findById(3L)).thenReturn(Optional.of(
            new User(3L, "举报人", "pw", "READER", null)));
        when(userRepository.findById(7L)).thenReturn(Optional.of(
            new User(7L, "被举报人", "pw", "READER", null)));

        ResolveReportRequest req = new ResolveReportRequest();
        req.setAction("dismiss");

        reportService.resolveReport(1L, 2L, req);

        verify(reviewRepository, never()).delete(any());
        verify(reportRepository).save(argThat(r -> "dismissed".equals(r.getStatus())));
        verify(notificationService, times(2)).createNotification(
            anyLong(), anyString(), anyString(), anyString());
    }

    @Test
    void shouldGetReports() {
        Report report = Report.builder().id(1L).reviewId(10L).reporterId(3L)
            .reason("spam").status("pending").createdAt(LocalDateTime.now()).build();
        Page<Report> page = new PageImpl<>(List.of(report));
        when(reportRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class)))
            .thenReturn(page);
        Review review = Review.builder().id(10L).userId(7L).content("待审核").bookId(5L).build();
        when(reviewRepository.findById(10L)).thenReturn(Optional.of(review));
        when(userRepository.findById(3L)).thenReturn(Optional.of(
            new User(3L, "举报人", "pw", "READER", null)));
        when(userRepository.findById(7L)).thenReturn(Optional.of(
            new User(7L, "被举报人", "pw", "READER", null)));

        PageResult<ReportResponse> result = reportService.getReports(null, 1, 10);

        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getReporterName()).isEqualTo("举报人");
        assertThat(result.getRecords().get(0).getReviewAuthorName()).isEqualTo("被举报人");
    }
}
```

- [ ] **Step 2: 运行测试验证失败**

Run: `cd library-server && ./mvnw test -Dtest=ReportServiceTest -q`
Expected: FAIL (ReportService + NotificationService classes not found)

- [ ] **Step 3: 实现 ReportService 和 NotificationService**

```java
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
import java.time.format.DateTimeFormatter;
import java.util.*;

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

        if ("other".equals(req.getReason()) &&
            (req.getDetail() == null || req.getDetail().trim().isEmpty())) {
            throw new IllegalArgumentException("选择"其他"时必须填写补充说明");
        }

        Report report = Report.builder()
            .reviewId(reviewId).reporterId(reporterId)
            .reason(req.getReason()).detail(req.getDetail())
            .status("pending").build();
        reportRepository.save(report);
    }

    public PageResult<ReportResponse> getReports(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<Report> reportPage;
        if (status != null && !status.isEmpty()) {
            reportPage = reportRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        } else {
            reportPage = reportRepository.findAllByOrderByCreatedAtDesc(pageable);
        }

        List<ReportResponse> records = reportPage.getContent().stream()
            .map(this::toResponse).collect(java.util.stream.Collectors.toList());

        return PageResult.<ReportResponse>builder()
            .records(records).total(reportPage.getTotalElements())
            .page(page).size(size).build();
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

        String reporterName = userRepository.findById(report.getReporterId())
            .map(User::getUsername).orElse("未知用户");
        String reviewAuthorName = userRepository.findById(review.getUserId())
            .map(User::getUsername).orElse("未知用户");
        String bookTitle = bookRepository.findById(review.getBookId())
            .map(b -> b.getTitle()).orElse("未知图书");

        String reasonLabel = reasonLabel(report.getReason());
        String reviewSnippet = review.getContent().length() > 50
            ? review.getContent().substring(0, 50) + "..."
            : review.getContent();

        if ("delete".equals(req.getAction())) {
            reviewRepository.delete(review);
            report.setStatus("deleted");

            notificationService.createNotification(review.getUserId(), "report_result",
                "你的书评被举报，已被管理员删除",
                "你在《" + bookTitle + "》中的书评「" + reviewSnippet + "」被举报「" + reasonLabel + "」，管理员审核后已删除该评论。");

            notificationService.createNotification(report.getReporterId(), "report_result",
                "你举报的书评已被删除",
                "你在《" + bookTitle + "》中举报的书评「" + reviewSnippet + "」管理员审核后已删除处理。");

        } else if ("dismiss".equals(req.getAction())) {
            report.setStatus("dismissed");

            notificationService.createNotification(review.getUserId(), "report_result",
                "你的书评被举报，举报已被驳回",
                "你在《" + bookTitle + "》中的书评「" + reviewSnippet + "」被举报「" + reasonLabel + "」，管理员审核后驳回了举报。");

            notificationService.createNotification(report.getReporterId(), "report_result",
                "你举报的书评举报被驳回",
                "你在《" + bookTitle + "》中举报的书评「" + reviewSnippet + "」管理员审核后驳回了举报。");
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
                reviewContent = review.getContent().length() > 100
                    ? review.getContent().substring(0, 100) + "..."
                    : review.getContent();
                reviewAuthorId = review.getUserId();
                bookTitle = bookRepository.findById(review.getBookId())
                    .map(b -> b.getTitle()).orElse("");
            }
        } catch (Exception ignored) {}

        String reporterName = userRepository.findById(report.getReporterId())
            .map(User::getUsername).orElse("未知用户");
        String reviewAuthorName = reviewAuthorId != null
            ? userRepository.findById(reviewAuthorId).map(User::getUsername).orElse("未知用户")
            : "未知用户";

        return ReportResponse.builder()
            .id(report.getId()).reviewId(report.getReviewId())
            .reviewContent(reviewContent).bookTitle(bookTitle)
            .reporterName(reporterName).reviewAuthorName(reviewAuthorName)
            .reason(report.getReason()).detail(report.getDetail())
            .status(report.getStatus()).adminId(report.getAdminId())
            .adminNote(report.getAdminNote())
            .createdAt(report.getCreatedAt()).resolvedAt(report.getResolvedAt())
            .build();
    }

    private String reasonLabel(String reason) {
        return switch (reason) {
            case "spam" -> "垃圾广告";
            case "abuse" -> "人身攻击";
            case "fake" -> "虚假信息";
            case "violation" -> "违规内容";
            case "other" -> "其他";
            default -> reason;
        };
    }
}
```

```java
package com.library.service;

import com.library.dto.NotificationResponse;
import com.library.dto.PageResult;
import com.library.entity.Notification;
import com.library.repository.NotificationRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public PageResult<NotificationResponse> getNotifications(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Notification> notifPage = notificationRepository
            .findByUserIdOrderByCreatedAtDesc(userId, pageable);

        List<NotificationResponse> records = notifPage.getContent().stream()
            .map(this::toResponse).collect(Collectors.toList());

        return PageResult.<NotificationResponse>builder()
            .records(records).total(notifPage.getTotalElements())
            .page(page).size(size).build();
    }

    public int getUnreadCount(Long userId) {
        return notificationRepository.countUnread(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            if (n.getUserId().equals(userId)) {
                n.setIsRead(1);
                notificationRepository.save(n);
            }
        });
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId);
    }

    @Transactional
    public void createNotification(Long userId, String type, String title, String content) {
        Notification n = new Notification(userId, type, title, content);
        notificationRepository.save(n);
    }

    private NotificationResponse toResponse(Notification n) {
        NotificationResponse r = new NotificationResponse();
        r.setId(n.getId());
        r.setType(n.getType());
        r.setTitle(n.getTitle());
        r.setContent(n.getContent());
        r.setRead(n.getIsRead() == 1);
        r.setCreatedAt(n.getCreatedAt());
        return r;
    }
}
```

- [ ] **Step 4: 运行测试验证通过**

Run: `cd library-server && ./mvnw test -Dtest=ReportServiceTest -q`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add library-server/src/main/java/com/library/service/ReportService.java library-server/src/main/java/com/library/service/NotificationService.java library-server/src/test/java/com/library/service/ReportServiceTest.java
git commit -m "feat: add ReportService and NotificationService"
```

---

### Task 5: 创建 Controller 层 + 集成测试（TDD）

**Files:**
- Create: `library-server/src/test/java/com/library/controller/ReportControllerTest.java`
- Create: `library-server/src/main/java/com/library/controller/ReportController.java`
- Create: `library-server/src/main/java/com/library/controller/NotificationController.java`
- Create: `library-server/src/main/java/com/library/controller/AdminReportController.java`

- [ ] **Step 1: 编写 ReportControllerTest**

```java
package com.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.dto.*;
import com.library.service.NotificationService;
import com.library.service.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({ReportController.class, NotificationController.class, AdminReportController.class})
class ReportControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean ReportService reportService;
    @MockBean NotificationService notificationService;

    @Test
    void shouldCreateReport() throws Exception {
        ReportRequest req = new ReportRequest();
        req.setReason("spam");

        mvc.perform(post("/api/reviews/1/report")
                .requestAttr("userId", 5L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldGetNotifications() throws Exception {
        NotificationResponse n = new NotificationResponse();
        n.setId(1L); n.setTitle("测试"); n.setType("report_result");
        PageResult<NotificationResponse> page = PageResult.<NotificationResponse>builder()
            .records(List.of(n)).total(1).page(1).size(20).build();
        when(notificationService.getNotifications(5L, 1, 20)).thenReturn(page);

        mvc.perform(get("/api/notifications").requestAttr("userId", 5L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.records[0].title").value("测试"));
    }

    @Test
    void shouldGetUnreadCount() throws Exception {
        when(notificationService.getUnreadCount(5L)).thenReturn(3);
        mvc.perform(get("/api/notifications/unread-count").requestAttr("userId", 5L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").value(3));
    }

    @Test
    void shouldMarkAsRead() throws Exception {
        mvc.perform(put("/api/notifications/1/read").requestAttr("userId", 5L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldMarkAllAsRead() throws Exception {
        mvc.perform(put("/api/notifications/read-all").requestAttr("userId", 5L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldGetReportsAsAdmin() throws Exception {
        PageResult<ReportResponse> page = PageResult.<ReportResponse>builder()
            .records(List.of()).total(0).page(1).size(10).build();
        when(reportService.getReports(isNull(), eq(1), eq(10))).thenReturn(page);

        mvc.perform(get("/api/admin/reports"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldResolveReportAsAdmin() throws Exception {
        ResolveReportRequest req = new ResolveReportRequest();
        req.setAction("delete");

        mvc.perform(put("/api/admin/reports/1/resolve")
                .requestAttr("userId", 2L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }
}
```

- [ ] **Step 2: 运行测试验证失败**

Run: `cd library-server && ./mvnw test -Dtest=ReportControllerTest -q`
Expected: FAIL (controllers not found)

- [ ] **Step 3: 实现三个 Controller**

```java
package com.library.controller;

import com.library.dto.*;
import com.library.service.ReportService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/api/reviews/{id}/report")
    public ApiResponse<Void> createReport(
            @PathVariable Long id,
            @Valid @RequestBody ReportRequest request,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        reportService.createReport(id, userId, request);
        return ApiResponse.success(null);
    }
}
```

```java
package com.library.controller;

import com.library.dto.*;
import com.library.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ApiResponse<PageResult<NotificationResponse>> listNotifications(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.success(notificationService.getNotifications(userId, page, size));
    }

    @GetMapping("/unread-count")
    public ApiResponse<Integer> getUnreadCount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.success(notificationService.getUnreadCount(userId));
    }

    @PutMapping("/{id}/read")
    public ApiResponse<Void> markAsRead(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        notificationService.markAsRead(id, userId);
        return ApiResponse.success(null);
    }

    @PutMapping("/read-all")
    public ApiResponse<Void> markAllAsRead(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        notificationService.markAllAsRead(userId);
        return ApiResponse.success(null);
    }
}
```

```java
package com.library.controller;

import com.library.dto.*;
import com.library.service.ReportService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminReportController {

    private final ReportService reportService;

    public AdminReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/reports")
    public ApiResponse<PageResult<ReportResponse>> listReports(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(reportService.getReports(status, page, size));
    }

    @PutMapping("/reports/{id}/resolve")
    public ApiResponse<Void> resolveReport(
            @PathVariable Long id,
            @Valid @RequestBody ResolveReportRequest request,
            HttpServletRequest httpRequest) {
        Long adminId = (Long) httpRequest.getAttribute("userId");
        reportService.resolveReport(id, adminId, request);
        return ApiResponse.success(null);
    }
}
```

- [ ] **Step 4: 运行测试验证通过**

Run: `cd library-server && ./mvnw test -Dtest=ReportControllerTest -q`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add library-server/src/main/java/com/library/controller/ReportController.java library-server/src/main/java/com/library/controller/NotificationController.java library-server/src/main/java/com/library/controller/AdminReportController.java library-server/src/test/java/com/library/controller/ReportControllerTest.java
git commit -m "feat: add report, notification and admin report controllers"
```

---

### Task 6: 修改 JWT Filter 扩展 /api/notifications 认证

**Files:**
- Modify: `library-server/src/main/java/com/library/config/JwtFilter.java`

- [ ] **Step 1: 更新 needsAuth 条件**

当前 needsAuth 条件为：
```java
boolean needsAuth = path.equals("/api/auth/me")
    || path.startsWith("/api/bookshelf")
    || (path.startsWith("/api/reviews") && !"GET".equalsIgnoreCase(req.getMethod()))
    || (path.matches("/api/books/\\d+/reviews") && !"GET".equalsIgnoreCase(req.getMethod()));
```

在其后新增一行：
```java
    || path.startsWith("/api/notifications");
```

- [ ] **Step 2: 运行已有测试确保不回归**

Run: `cd library-server && ./mvnw test -q`
Expected: 无新增失败

- [ ] **Step 3: Commit**

```bash
git add library-server/src/main/java/com/library/config/JwtFilter.java
git commit -m "feat: extend JWT filter to cover /api/notifications"
```

---

### Task 7: 创建前端 API 模块

**Files:**
- Create: `reader-app/src/api/report.js`
- Create: `admin-app/src/api/report.js`

- [ ] **Step 1: 创建读者端 report.js（举报 + 通知 API）**

```javascript
import api from './index'

export function reportReview(reviewId, data) {
  return api.post(`/reviews/${reviewId}/report`, data)
}

export function getNotifications(params = {}) {
  return api.get('/notifications', { params })
}

export function getUnreadCount() {
  return api.get('/notifications/unread-count')
}

export function markAsRead(notificationId) {
  return api.put(`/notifications/${notificationId}/read`)
}

export function markAllAsRead() {
  return api.put('/notifications/read-all')
}
```

- [ ] **Step 2: 创建管理端 report.js（举报管理 API）**

管理端使用 `adminApi`（baseURL `/api/admin`）：

```javascript
import { adminApi } from './index'

export function getReports(params) {
  return adminApi.get('/reports', { params })
}

export function resolveReport(id, data) {
  return adminApi.put(`/reports/${id}/resolve`, data)
}
```

- [ ] **Step 3: Commit**

```bash
git add reader-app/src/api/report.js admin-app/src/api/report.js
git commit -m "feat: add report and notification API modules"
```

---

### Task 8: 创建 ReportDialog 组件（读者端）

**Files:**
- Create: `reader-app/src/components/ReportDialog.vue`

- [ ] **Step 1: 创建 ReportDialog.vue**

```vue
<template>
  <Transition name="fade">
    <div v-if="visible" class="dialog-overlay" @click.self="$emit('close')">
      <div class="dialog-card">
        <h3 class="dialog-title">举报书评</h3>
        <div class="dialog-body">
          <div class="reason-list">
            <label
              v-for="opt in reasonOptions"
              :key="opt.value"
              class="reason-item"
              :class="{ selected: selectedReason === opt.value }"
            >
              <input
                type="radio"
                :value="opt.value"
                v-model="selectedReason"
                class="reason-radio"
              />
              <span class="reason-label">{{ opt.label }}</span>
            </label>
          </div>
          <div v-if="selectedReason === 'other'" class="detail-area">
            <textarea
              v-model="detail"
              class="detail-input"
              placeholder="请描述具体理由（必填，200字以内）"
              maxlength="200"
              rows="3"
            ></textarea>
          </div>
        </div>
        <div class="dialog-actions">
          <button class="btn-cancel" @click="$emit('close')">取消</button>
          <button
            class="btn-submit"
            :disabled="!canSubmit || submitting"
            @click="doSubmit"
          >{{ submitting ? '提交中...' : '提交举报' }}</button>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { ref, computed } from 'vue'
import { reportReview } from '../api/report'

const props = defineProps({ reviewId: { type: Number, required: true }, visible: Boolean })
const emit = defineEmits(['close', 'done'])

const selectedReason = ref('')
const detail = ref('')
const submitting = ref(false)

const reasonOptions = [
  { value: 'spam', label: '垃圾广告' },
  { value: 'abuse', label: '人身攻击' },
  { value: 'fake', label: '虚假信息' },
  { value: 'violation', label: '违规内容' },
  { value: 'other', label: '其他' }
]

const canSubmit = computed(() => {
  if (!selectedReason.value) return false
  if (selectedReason.value === 'other' && !detail.value.trim()) return false
  return true
})

async function doSubmit() {
  if (!canSubmit.value) return
  submitting.value = true
  try {
    await reportReview(props.reviewId, {
      reason: selectedReason.value,
      detail: selectedReason.value === 'other' ? detail.value.trim() : undefined
    })
    emit('done')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.dialog-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,0.35);
  display: flex; align-items: center; justify-content: center;
  z-index: 1000;
}
.dialog-card {
  background: var(--color-card-bg, #fff);
  border-radius: var(--radius, 8px);
  padding: 24px; width: 400px; max-width: 90vw;
  box-shadow: var(--shadow-lg);
}
.dialog-title {
  font-size: 16px; font-family: var(--font-serif);
  color: var(--color-text, #4a3d2f); margin-bottom: 16px;
  font-weight: 600;
}
.reason-list { display: flex; flex-direction: column; gap: 2px; }
.reason-item {
  display: flex; align-items: center; gap: 8px;
  padding: 10px 12px; border-radius: var(--radius-sm, 6px);
  cursor: pointer; transition: background 0.2s;
}
.reason-item:hover { background: var(--color-accent-light, #f0ebe0); }
.reason-item.selected { background: var(--color-accent-light, #f0ebe0); }
.reason-radio { accent-color: var(--color-primary, #c9a96e); }
.reason-label { font-size: 13px; color: var(--color-text, #4a3d2f); }
.detail-area { margin-top: 12px; }
.detail-input {
  width: 100%; padding: 8px 10px;
  border: 1px solid #e0dbd0; border-radius: var(--radius-sm, 6px);
  font-size: 12px; resize: vertical; font-family: inherit; outline: none;
}
.detail-input:focus { border-color: var(--color-primary, #c9a96e); }
.dialog-actions { display: flex; gap: 10px; justify-content: flex-end; margin-top: 20px; }
.btn-cancel {
  padding: 6px 16px; background: var(--color-bg, #fafaf7);
  color: var(--color-text-secondary, #8b8070);
  border: 1px solid #e0dbd0; border-radius: var(--radius-sm, 6px);
  font-size: 13px; cursor: pointer;
}
.btn-submit {
  padding: 6px 16px; background: var(--color-primary, #c9a96e); color: #fff;
  border: none; border-radius: var(--radius-sm, 6px); font-size: 13px; cursor: pointer;
}
.btn-submit:disabled { opacity: 0.5; cursor: not-allowed; }
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
```

- [ ] **Step 2: 验证前端编译**

Run: `cd reader-app && npm run build`
Expected: Build succeeds

- [ ] **Step 3: Commit**

```bash
git add reader-app/src/components/ReportDialog.vue
git commit -m "feat: add ReportDialog component"
```

---

### Task 9: 创建 Inbox 收件箱页面（读者端）

**Files:**
- Create: `reader-app/src/views/Inbox.vue`

- [ ] **Step 1: 创建 Inbox.vue**

```vue
<template>
  <div class="inbox-page">
    <div class="inbox-header">
      <h2 class="inbox-title">收件箱</h2>
      <button v-if="totalCount > 0" class="btn-read-all" @click="onReadAll">全部已读</button>
    </div>

    <div v-if="loading" class="inbox-loading">加载中...</div>
    <div v-else-if="notifications.length === 0" class="inbox-empty">暂无通知</div>
    <div v-else class="inbox-list">
      <div
        v-for="n in notifications"
        :key="n.id"
        class="inbox-item"
        :class="{ unread: !n.read }"
        @click="onClick(n)"
      >
        <div class="inbox-dot" v-if="!n.read"></div>
        <div class="inbox-body">
          <div class="inbox-item-title">{{ n.title }}</div>
          <div class="inbox-item-content">{{ n.content }}</div>
          <div class="inbox-item-time">{{ formatDate(n.createdAt) }}</div>
        </div>
      </div>
    </div>

    <div v-if="totalPages > 1" class="inbox-pagination">
      <button :disabled="page <= 1" @click="goPage(page - 1)">← 上一页</button>
      <span class="page-info">{{ page }} / {{ totalPages }}</span>
      <button :disabled="page >= totalPages" @click="goPage(page + 1)">下一页 →</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getNotifications, markAsRead, markAllAsRead } from '../api/report'

const notifications = ref([])
const loading = ref(false)
const page = ref(1)
const totalCount = ref(0)
const pageSize = 20

const totalPages = computed(() => Math.ceil(totalCount.value / pageSize) || 0)

onMounted(() => fetchNotifications())

async function fetchNotifications() {
  loading.value = true
  try {
    const data = await getNotifications({ page: page.value, size: pageSize })
    notifications.value = data.records
    totalCount.value = data.total
  } finally {
    loading.value = false
  }
}

async function onClick(n) {
  if (!n.read) {
    await markAsRead(n.id)
    n.read = true
  }
}

async function onReadAll() {
  await markAllAsRead()
  notifications.value.forEach(n => n.read = true)
}

function goPage(p) { page.value = p; fetchNotifications() }

function formatDate(dateStr) {
  if (!dateStr) return ''
  const now = new Date(); const date = new Date(dateStr)
  const diff = now - date
  const days = Math.floor(diff / 86400000)
  if (days < 1) {
    const hours = Math.floor(diff / 3600000)
    if (hours < 1) {
      const mins = Math.floor(diff / 60000)
      return mins < 1 ? '刚刚' : `${mins}分钟前`
    }
    return `${hours}小时前`
  }
  if (days < 30) return `${days}天前`
  return date.toLocaleDateString('zh-CN')
}
</script>

<style scoped>
.inbox-page { padding: 24px 32px; max-width: 700px; }
.inbox-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
.inbox-title {
  font-size: 20px; font-family: var(--font-serif);
  color: var(--color-text, #4a3d2f); font-weight: 600; letter-spacing: 2px;
}
.btn-read-all {
  padding: 5px 14px; background: var(--color-bg, #fafaf7);
  color: var(--color-text-secondary, #8b8070);
  border: 1px solid #e0dbd0; border-radius: var(--radius, 8px);
  font-size: 12px; cursor: pointer;
}
.inbox-loading, .inbox-empty { text-align: center; padding: 60px; color: var(--color-text-muted, #a09880); font-size: 13px; }
.inbox-item {
  display: flex; gap: 10px; padding: 16px;
  background: var(--color-card-bg, #fff);
  border: 1px solid var(--color-card-border, #ece8df);
  border-radius: var(--radius, 8px); margin-bottom: 8px;
  cursor: pointer; transition: border-color 0.2s;
}
.inbox-item:hover { border-color: var(--color-primary, #c9a96e); }
.inbox-item.unread { border-left: 3px solid var(--color-primary, #c9a96e); }
.inbox-dot { width: 8px; height: 8px; border-radius: 50%; background: var(--color-primary, #c9a96e); flex-shrink: 0; margin-top: 4px; }
.inbox-body { flex: 1; }
.inbox-item-title { font-size: 14px; font-weight: 500; color: var(--color-text, #4a3d2f); margin-bottom: 4px; }
.inbox-item-content { font-size: 12px; color: var(--color-text-secondary, #8b8070); line-height: 1.6; }
.inbox-item-time { font-size: 11px; color: var(--color-text-muted, #a09880); margin-top: 6px; }
.inbox-pagination { text-align: center; margin-top: 20px; display: flex; justify-content: center; gap: 12px; }
.inbox-pagination button {
  padding: 6px 14px; background: var(--color-bg, #fafaf7);
  color: var(--color-text-secondary, #8b8070);
  border: 1px solid #e0dbd0; border-radius: var(--radius, 8px);
  font-size: 12px; cursor: pointer;
}
.inbox-pagination button:disabled { color: #d0c8b4; cursor: not-allowed; }
.page-info { font-size: 13px; color: var(--color-text-muted, #a09880); }
</style>
```

- [ ] **Step 2: 验证前端编译**

Run: `cd reader-app && npm run build`
Expected: Build succeeds

- [ ] **Step 3: Commit**

```bash
git add reader-app/src/views/Inbox.vue
git commit -m "feat: add Inbox page for notifications"
```

---

### Task 10: 修改 ReviewItem 添加举报入口 + 路由和导航

**Files:**
- Modify: `reader-app/src/components/ReviewItem.vue` — 操作栏加举报
- Modify: `reader-app/src/router/index.js` — 新增 /inbox 路由
- Modify: `reader-app/src/components/BannerHeader.vue` — 加收件箱入口+红点

- [ ] **Step 1: ReviewItem 操作栏加「举报」**

在 ReviewItem.vue 模板的 `.review-meta` 中，在「回复」后面加一行：

```html
        <span class="meta-action" @click.stop="$emit('report', review.id)">举报</span>
```

同时新增 emit 声明，在 `defineEmits` 中加 `'report'`：

```javascript
const emit = defineEmits(['like', 'delete', 'edit', 'reply', 'report'])
```

注意：非自己的评论才显示举报（自己的评论已有编辑/删除），所以举报应放在 `v-if="isOwn"` 的相反条件中：

```html
        <span v-if="!isOwn" class="meta-action" @click.stop="$emit('report', review.id)">举报</span>
```

- [ ] **Step 2: 路由新增 /inbox**

在 `reader-app/src/router/index.js` 的 `routes` 数组中添加：

```javascript
import Inbox from '../views/Inbox.vue'

// 在 routes 数组中新增：
{ path: '/inbox', name: 'inbox', component: Inbox, meta: { requiresAuth: true } }
```

- [ ] **Step 3: BannerHeader 加收件箱入口+未读红点**

在 BannerHeader.vue 模板中，用户名前面加收件箱链接和红点：

```html
              <span class="nav-item" @click="$router.push('/inbox')" style="position:relative">
                收件箱
                <span v-if="unreadCount > 0" class="badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
              </span>
              <span class="nav-sep">|</span>
```

在 `<script setup>` 中添加：

```javascript
import { ref, onMounted } from 'vue'
import { getUnreadCount } from '../api/report'

const unreadCount = ref(0)

onMounted(async () => {
  if (isLoggedIn.value) {
    try {
      unreadCount.value = await getUnreadCount()
    } catch { /* ignore */ }
  }
})
```

CSS 红点样式：

```css
.badge {
  position: absolute; top: -6px; right: -10px;
  background: var(--color-danger, #c04040); color: #fff;
  font-size: 10px; padding: 1px 5px; border-radius: 10px;
  min-width: 16px; text-align: center; line-height: 16px;
}
```

- [ ] **Step 4: 验证前端编译**

Run: `cd reader-app && npm run build`
Expected: Build succeeds

- [ ] **Step 5: Commit**

```bash
git add reader-app/src/components/ReviewItem.vue reader-app/src/router/index.js reader-app/src/components/BannerHeader.vue
git commit -m "feat: add report button, inbox route, and notification badge"
```

---

### Task 11: 创建管理端 ReportManagement 页面 + 路由 + 导航

**Files:**
- Create: `admin-app/src/views/ReportManagement.vue`
- Modify: `admin-app/src/router/index.js`
- Modify: `admin-app/src/components/AdminHeader.vue`

- [ ] **Step 1: 创建 ReportManagement.vue**

```vue
<template>
  <div class="report-page">
    <h2 class="page-title">举报管理</h2>

    <div class="filter-bar">
      <select v-model="statusFilter" class="filter-select" @change="search">
        <option value="">全部</option>
        <option value="pending">待处理</option>
        <option value="deleted">已删除</option>
        <option value="dismissed">已驳回</option>
      </select>
    </div>

    <table class="data-table" v-if="reports.length > 0">
      <thead>
        <tr>
          <th>书评内容</th><th>所属图书</th><th>举报人</th><th>被举报人</th>
          <th>理由</th><th>状态</th><th>时间</th><th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="r in reports" :key="r.id">
          <td class="cell-content">{{ r.reviewContent || '(已删除)' }}</td>
          <td>{{ r.bookTitle }}</td>
          <td>{{ r.reporterName }}</td>
          <td>{{ r.reviewAuthorName }}</td>
          <td>{{ reasonLabel(r.reason) }}<span v-if="r.detail">: {{ r.detail }}</span></td>
          <td><span class="status-tag" :class="r.status">{{ statusLabel(r.status) }}</span></td>
          <td>{{ formatDate(r.createdAt) }}</td>
          <td class="cell-actions" v-if="r.status === 'pending'">
            <a @click="resolveReport(r, 'delete')">删除评论</a>
            <span class="sep">|</span>
            <a @click="resolveReport(r, 'dismiss')">驳回举报</a>
          </td>
          <td v-else class="cell-muted">已处理</td>
        </tr>
      </tbody>
    </table>
    <div v-else-if="!loading" class="empty-text">暂无举报</div>

    <div class="pagination" v-if="totalPages > 1">
      <button :disabled="page <= 1" @click="goPage(page - 1)">← 上一页</button>
      <span class="page-info">{{ page }} / {{ totalPages }}</span>
      <button :disabled="page >= totalPages" @click="goPage(page + 1)">下一页 →</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getReports, resolveReport } from '../api/report'

const reports = ref([])
const loading = ref(false)
const page = ref(1)
const totalCount = ref(0)
const statusFilter = ref('')
const pageSize = 10

const totalPages = computed(() => Math.ceil(totalCount.value / pageSize) || 0)

onMounted(() => fetchReports())

async function fetchReports() {
  loading.value = true
  try {
    const data = await getReports({
      status: statusFilter.value || undefined,
      page: page.value, size: pageSize
    })
    reports.value = data.records
    totalCount.value = data.total
  } finally { loading.value = false }
}

async function resolveReport(report, action) {
  const label = action === 'delete' ? '删除评论' : '驳回举报'
  if (!confirm(`确定${label}吗？`)) return
  await resolveReport(report.id, { action })
  fetchReports()
}

function search() { page.value = 1; fetchReports() }
function goPage(p) { page.value = p; fetchReports() }

function reasonLabel(r) {
  const map = { spam: '垃圾广告', abuse: '人身攻击', fake: '虚假信息', violation: '违规内容', other: '其他' }
  return map[r] || r
}

function statusLabel(s) {
  const map = { pending: '待处理', deleted: '已删除', dismissed: '已驳回' }
  return map[s] || s
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN')
}
</script>

<style scoped>
.report-page { padding: 20px 28px; }
.page-title {
  font-size: 20px; font-family: var(--font-serif);
  color: var(--color-text); letter-spacing: 2px; margin-bottom: 16px;
}
.filter-bar { margin-bottom: 14px; }
.filter-select {
  padding: 6px 12px; border: 1px solid #e0dbd0; border-radius: var(--radius);
  font-size: 12px; color: var(--color-text-secondary); outline: none;
}
.data-table { width: 100%; border-collapse: collapse; font-size: 12px; }
.data-table th {
  text-align: left; padding: 10px 12px; color: var(--color-text-secondary);
  font-weight: 500; border-bottom: 2px solid var(--color-border);
}
.data-table td { padding: 10px 12px; border-bottom: 1px solid var(--color-accent-light); }
.cell-content { max-width: 200px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.status-tag { padding: 2px 8px; border-radius: 2px; font-size: 11px; }
.status-tag.pending { background: #fff3e0; color: #c08840; }
.status-tag.deleted { background: #ffebee; color: #c04040; }
.status-tag.dismissed { background: #e8f5e9; color: #5b8c5a; }
.cell-actions a { cursor: pointer; color: var(--color-text-secondary); }
.cell-actions a:hover { color: var(--color-primary); }
.cell-actions a:first-child:hover { color: var(--color-danger); }
.sep { color: var(--color-border); margin: 0 6px; }
.cell-muted { color: var(--color-text-muted); }
.empty-text { text-align: center; padding: 40px; color: var(--color-text-muted); font-size: 13px; }
.pagination { text-align: center; margin-top: 20px; display: flex; justify-content: center; gap: 12px; }
.pagination button {
  padding: 6px 14px; background: var(--color-bg, #fafaf7);
  color: var(--color-text-secondary); border: 1px solid #e0dbd0;
  border-radius: var(--radius); font-size: 12px; cursor: pointer;
}
.pagination button:disabled { color: #d0c8b4; cursor: not-allowed; }
.page-info { font-size: 13px; color: var(--color-text-muted); }
</style>
```

- [ ] **Step 2: 管理端路由新增 /reports**

在 `admin-app/src/router/index.js` 中添加 import 和路由：

```javascript
import ReportManagement from '../views/ReportManagement.vue'
// 在 routes 数组中新增：
{ path: '/reports', name: 'report-list', component: ReportManagement }
```

- [ ] **Step 3: AdminHeader 导航加「举报管理」**

在 AdminHeader.vue 模板中，「新增管理员」之前插入：

```html
      <router-link class="nav-link desktop-only" to="/reports">举报管理</router-link>
      <span class="nav-divider desktop-only">|</span>
```

同时在移动端菜单（`.mobile-menu`）中同样插入：

```html
      <router-link class="mobile-menu-item" to="/reports" @click="menuOpen = false">举报管理</router-link>
```

- [ ] **Step 4: 验证前端编译**

Run: `cd reader-app && npm run build && cd ../admin-app && npm run build`
Expected: Both build succeed

- [ ] **Step 5: Commit**

```bash
git add admin-app/src/views/ReportManagement.vue admin-app/src/router/index.js admin-app/src/components/AdminHeader.vue
git commit -m "feat: add ReportManagement page with route and navigation"
```

---

### Task 12: 运行全部测试并验证

- [ ] **Step 1: 运行后端书评+举报相关测试**

Run: `cd library-server && ./mvnw test -Dtest=ReviewServiceTest,ReviewControllerTest,ReportServiceTest,ReportControllerTest -q`
Expected: All tests pass

- [ ] **Step 2: 运行全部后端测试**

Run: `cd library-server && ./mvnw test -q`
Expected: 无新增失败（预存的 ApplicationContext 加载错误不计）

- [ ] **Step 3: 验证前后端构建**

Run: `cd reader-app && npm run build && cd ../admin-app && npm run build`
Expected: Both build succeed

- [ ] **Step 4: Commit（如有修复）**

```bash
git add -A && git commit -m "chore: finalize report and moderation system" || echo "no changes"
```

---

## Plan Self-Review

**1. Spec coverage:**
- reports 表 + notifications 表 → Task 1
- Repository 层 → Task 2
- DTOs (ReportRequest, ReportResponse, ResolveReportRequest, NotificationResponse) → Task 3
- ReportService + NotificationService + 测试 → Task 4
- ReportController + NotificationController + AdminReportController + 测试 → Task 5
- JWT Filter 扩展 → Task 6
- 前端 API 模块 → Task 7
- ReportDialog 举报弹窗 → Task 8
- Inbox 收件箱页面 → Task 9
- ReviewItem 加举报按钮 + 路由 + BannerHeader 红点 → Task 10
- 管理端 ReportManagement + 路由 + AdminHeader 导航 → Task 11
- 预设理由 (spam/abuse/fake/violation/other) + other 需补充说明 → Task 8
- UNIQUE(review_id, reporter_id) → Task 1
- 不能举报自己 → Task 4 后端校验
- 举报人+被举报人双方收通知 → Task 4 resolveReport
- 举报期间评论保持可见 → 处理逻辑仅在 resolve 时操作

**2. Placeholder scan:** 无 TBD/TODO，所有步骤包含完整代码。

**3. Type consistency:**
- ReportRequest.reason/detail → Task 3/4/5/8 一致
- ResolveReportRequest.action/note → Task 3/4/5/11 一致
- NotificationResponse → Task 3/4/5/9 一致
- ReportResponse 字段 → Task 3/4/11 一致
