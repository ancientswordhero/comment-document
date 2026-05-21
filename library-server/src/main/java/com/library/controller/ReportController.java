package com.library.controller;

import com.library.dto.*;
import com.library.service.ReportService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) { this.reportService = reportService; }

    @PostMapping("/api/reviews/{id}/report")
    public ApiResponse<Void> createReport(@PathVariable Long id,
            @Valid @RequestBody ReportRequest request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        reportService.createReport(id, userId, request);
        return ApiResponse.success(null);
    }
}
