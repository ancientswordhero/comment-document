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
    @Mock BookRepository bookRepository;
    @InjectMocks ReportService reportService;

    @Test
    void shouldCreateReport() {
        ReportRequest req = new ReportRequest();
        req.setReason("spam");
        when(reportRepository.existsByReviewIdAndReporterId(1L, 5L)).thenReturn(false);
        Review review = Review.builder().id(1L).userId(10L).content("被举报内容").build();
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reportRepository.save(any(Report.class))).thenAnswer(inv -> {
            Report r = inv.getArgument(0); r.setId(100L); return r;
        });
        reportService.createReport(1L, 5L, req);
        verify(reportRepository).save(any(Report.class));
    }

    @Test
    void shouldRejectDuplicateReport() {
        when(reportRepository.existsByReviewIdAndReporterId(1L, 5L)).thenReturn(true);
        ReportRequest req = new ReportRequest(); req.setReason("spam");
        assertThatThrownBy(() -> reportService.createReport(1L, 5L, req))
            .isInstanceOf(IllegalStateException.class).hasMessageContaining("已经举报过");
    }

    @Test
    void shouldRejectSelfReport() {
        when(reportRepository.existsByReviewIdAndReporterId(1L, 5L)).thenReturn(false);
        Review review = Review.builder().id(1L).userId(5L).content("自己的评论").build();
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        ReportRequest req = new ReportRequest(); req.setReason("spam");
        assertThatThrownBy(() -> reportService.createReport(1L, 5L, req))
            .isInstanceOf(IllegalStateException.class).hasMessageContaining("不能举报自己的书评");
    }

    @Test
    void shouldRequireDetailWhenReasonIsOther() {
        when(reportRepository.existsByReviewIdAndReporterId(1L, 5L)).thenReturn(false);
        Review review = Review.builder().id(1L).userId(10L).content("test").build();
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        ReportRequest req = new ReportRequest(); req.setReason("other");
        assertThatThrownBy(() -> reportService.createReport(1L, 5L, req))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("必须填写补充说明");
    }

    @Test
    void shouldResolveWithDelete() {
        Report report = Report.builder().id(1L).reviewId(10L).reporterId(3L).status("pending").reason("spam").build();
        when(reportRepository.findById(1L)).thenReturn(Optional.of(report));
        Review review = Review.builder().id(10L).userId(7L).content("违规内容").bookId(5L).build();
        when(reviewRepository.findById(10L)).thenReturn(Optional.of(review));
        when(userRepository.findById(3L)).thenReturn(Optional.of(new User(3L, "举报人", "pw", "READER", null)));
        when(userRepository.findById(7L)).thenReturn(Optional.of(new User(7L, "被举报人", "pw", "READER", null)));

        ResolveReportRequest req = new ResolveReportRequest(); req.setAction("delete");
        reportService.resolveReport(1L, 2L, req);

        verify(reviewRepository).delete(review);
        verify(reportRepository).save(argThat(r -> "deleted".equals(r.getStatus())));
        verify(notificationService, times(2)).createNotification(anyLong(), anyString(), anyString(), anyString());
    }

    @Test
    void shouldResolveWithDismiss() {
        Report report = Report.builder().id(1L).reviewId(10L).reporterId(3L).status("pending").reason("spam").build();
        when(reportRepository.findById(1L)).thenReturn(Optional.of(report));
        Review review = Review.builder().id(10L).userId(7L).content("正常内容").bookId(5L).build();
        when(reviewRepository.findById(10L)).thenReturn(Optional.of(review));
        when(userRepository.findById(3L)).thenReturn(Optional.of(new User(3L, "举报人", "pw", "READER", null)));
        when(userRepository.findById(7L)).thenReturn(Optional.of(new User(7L, "被举报人", "pw", "READER", null)));

        ResolveReportRequest req = new ResolveReportRequest(); req.setAction("dismiss");
        reportService.resolveReport(1L, 2L, req);

        verify(reviewRepository, never()).delete(any());
        verify(reportRepository).save(argThat(r -> "dismissed".equals(r.getStatus())));
        verify(notificationService, times(2)).createNotification(anyLong(), anyString(), anyString(), anyString());
    }

    @Test
    void shouldGetReports() {
        Report report = Report.builder().id(1L).reviewId(10L).reporterId(3L).reason("spam").status("pending").createdAt(LocalDateTime.now()).build();
        Page<Report> page = new PageImpl<>(List.of(report));
        when(reportRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class))).thenReturn(page);
        Review review = Review.builder().id(10L).userId(7L).content("待审核").bookId(5L).build();
        when(reviewRepository.findById(10L)).thenReturn(Optional.of(review));
        when(userRepository.findById(3L)).thenReturn(Optional.of(new User(3L, "举报人", "pw", "READER", null)));
        when(userRepository.findById(7L)).thenReturn(Optional.of(new User(7L, "被举报人", "pw", "READER", null)));

        PageResult<ReportResponse> result = reportService.getReports(null, 1, 10);

        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getReporterName()).isEqualTo("举报人");
        assertThat(result.getRecords().get(0).getReviewAuthorName()).isEqualTo("被举报人");
    }
}
