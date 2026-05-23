# Admin Report Notification Badge

**Date:** 2026-05-23

## Summary

Add a pending-report count badge on the "举报管理" nav link in admin-app, so admins see new reports without manually checking the page.

## Current Behavior

- Report creation silently persists to DB
- AdminHeader has no indicator for new/pending reports
- Admin must navigate to /reports to discover new reports

## Target Behavior

- AdminHeader polls pending report count every 30s
- Badge (red circle with number) displayed on "举报管理" link when count > 0
- Clicking the link navigates to /reports as usual

## Changes

### 1. Backend: `AdminReportController.java`

Add endpoint `GET /api/admin/reports/pending-count`:

```java
@GetMapping("/pending-count")
public ResponseEntity<ApiResponse<Map<String, Long>>> getPendingCount() {
    long count = reportService.getPendingCount();
    return ResponseEntity.ok(ApiResponse.success(Map.of("count", count)));
}
```

### 2. Backend: `ReportService.java`

Add method `getPendingCount()` — `SELECT COUNT(*) FROM reports WHERE status = 'PENDING'`.

### 3. Backend: `ReportRepository.java`

Add `long countByStatus(String status)` (Spring Data derived method).

### 4. Frontend: `admin-app/src/api/report.js`

Add `getPendingCount()` calling `GET /api/admin/reports/pending-count`.

### 5. Frontend: `AdminHeader.vue`

- Add `<span class="badge">` next to "举报管理" link, shown when `pendingCount > 0`
- `onMounted`: fetch count immediately, then `setInterval(fetch, 30000)`
- `onUnmounted`: `clearInterval` cleanup

## Unchanged

- ReportService.createReport (no notification integration)
- Notification system
- reader-app
- Existing /api/admin/reports endpoint
