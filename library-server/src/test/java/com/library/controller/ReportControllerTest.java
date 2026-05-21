package com.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.config.FileUploadConfig;
import com.library.config.JwtFilter;
import com.library.config.JwtUtil;
import com.library.config.SecurityConfig;
import com.library.config.WebConfig;
import com.library.dto.*;
import com.library.entity.User;
import com.library.service.NotificationService;
import com.library.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({ReportController.class, NotificationController.class, AdminReportController.class})
@Import({JwtUtil.class, JwtFilter.class, SecurityConfig.class, WebConfig.class, FileUploadConfig.class})
class ReportControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired JwtUtil jwtUtil;
    @MockBean ReportService reportService;
    @MockBean NotificationService notificationService;

    private String userToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        User testUser = User.builder().id(5L).username("testuser").role("USER").build();
        User testAdmin = User.builder().id(1L).username("admin").role("ADMIN").build();
        userToken = jwtUtil.generateToken(testUser);
        adminToken = jwtUtil.generateToken(testAdmin);
    }

    @Test
    void shouldCreateReport() throws Exception {
        ReportRequest req = new ReportRequest(); req.setReason("spam");
        mvc.perform(post("/api/reviews/1/report")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldGetNotifications() throws Exception {
        NotificationResponse n = new NotificationResponse();
        n.setId(1L); n.setTitle("测试"); n.setType("report_result");
        PageResult<NotificationResponse> page = PageResult.<NotificationResponse>builder()
            .records(List.of(n)).total(1).page(1).size(20).build();
        when(notificationService.getNotifications(5L, 1, 20)).thenReturn(page);
        mvc.perform(get("/api/notifications").requestAttr("userId", 5L))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.records[0].title").value("测试"));
    }

    @Test
    void shouldGetUnreadCount() throws Exception {
        when(notificationService.getUnreadCount(5L)).thenReturn(3);
        mvc.perform(get("/api/notifications/unread-count").requestAttr("userId", 5L))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data").value(3));
    }

    @Test
    void shouldMarkAsRead() throws Exception {
        mvc.perform(put("/api/notifications/1/read").requestAttr("userId", 5L))
            .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldMarkAllAsRead() throws Exception {
        mvc.perform(put("/api/notifications/read-all").requestAttr("userId", 5L))
            .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldGetReportsAsAdmin() throws Exception {
        PageResult<ReportResponse> page = PageResult.<ReportResponse>builder()
            .records(List.of()).total(0).page(1).size(10).build();
        when(reportService.getReports(isNull(), eq(1), eq(10))).thenReturn(page);
        mvc.perform(get("/api/admin/reports")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldResolveReportAsAdmin() throws Exception {
        ResolveReportRequest req = new ResolveReportRequest(); req.setAction("delete");
        mvc.perform(put("/api/admin/reports/1/resolve")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(200));
    }
}
