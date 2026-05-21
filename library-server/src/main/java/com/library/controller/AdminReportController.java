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

    public AdminReportController(ReportService reportService) { this.reportService = reportService; }

    @GetMapping("/reports")
    public ApiResponse<PageResult<ReportResponse>> listReports(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(reportService.getReports(status, page, size));
    }

    @PutMapping("/reports/{id}/resolve")
    public ApiResponse<Void> resolveReport(@PathVariable Long id,
            @Valid @RequestBody ResolveReportRequest request, HttpServletRequest httpRequest) {
        Long adminId = (Long) httpRequest.getAttribute("userId");
        reportService.resolveReport(id, adminId, request);
        return ApiResponse.success(null);
    }
}
