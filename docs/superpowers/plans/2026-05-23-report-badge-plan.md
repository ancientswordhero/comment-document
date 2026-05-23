# Admin Report Notification Badge — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a pending-report count badge on the admin "举报管理" nav link with 30s polling.

**Architecture:** Backend adds a lightweight `GET /api/admin/reports/pending-count` endpoint backed by a Spring Data `countByStatus` query. Frontend AdminHeader polls it on a 30s interval and shows a red badge when count > 0. No changes to notification system or existing report endpoints.

**Tech Stack:** Spring Boot 3.4 + JPA (backend), Vue 3 + Axios (admin-app)

---

### Task 1: Backend — ReportRepository add countByStatus

**Files:**
- Modify: `library-server/src/main/java/com/library/repository/ReportRepository.java:17`

- [ ] **Step 1: Add countByStatus method**

Add after line 17 (`boolean existsByReviewIdAndReporterId`):

```java
    long countByStatus(String status);
```

- [ ] **Step 2: Verify compilation**

Run: `cd library-server && ./mvnw compile -q 2>&1 | tail -3`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add library-server/src/main/java/com/library/repository/ReportRepository.java
git commit -m "feat: add countByStatus to ReportRepository"
```

---

### Task 2: Backend — ReportService add getPendingCount

**Files:**
- Modify: `library-server/src/main/java/com/library/service/ReportService.java:52`

- [ ] **Step 1: Add getPendingCount method**

Add after the `createReport` method (after line 52, before `getReports`):

```java
    public long getPendingCount() {
        return reportRepository.countByStatus("pending");
    }
```

- [ ] **Step 2: Verify compilation**

Run: `cd library-server && ./mvnw compile -q 2>&1 | tail -3`
Expected: BUILD SUCCESS

- [ ] **Step 3: Run existing tests**

Run: `cd library-server && ./mvnw test -q 2>&1 | tail -5`
Expected: All tests pass (no failures)

- [ ] **Step 4: Commit**

```bash
git add library-server/src/main/java/com/library/service/ReportService.java
git commit -m "feat: add getPendingCount to ReportService"
```

---

### Task 3: Backend — AdminReportController add pending-count endpoint

**Files:**
- Modify: `library-server/src/main/java/com/library/controller/AdminReportController.java:16`

- [ ] **Step 1: Add imports**

Add after existing imports:

```java
import java.util.Map;
```

- [ ] **Step 2: Add GET /pending-count endpoint**

Add after the constructor (after line 15, before `listReports`):

```java
    @GetMapping("/reports/pending-count")
    public ApiResponse<Map<String, Long>> pendingCount() {
        long count = reportService.getPendingCount();
        return ApiResponse.success(Map.of("count", count));
    }
```

- [ ] **Step 3: Verify compilation**

Run: `cd library-server && ./mvnw compile -q 2>&1 | tail -3`
Expected: BUILD SUCCESS

- [ ] **Step 4: Run existing tests**

Run: `cd library-server && ./mvnw test -q 2>&1 | tail -5`
Expected: All tests pass

- [ ] **Step 5: Commit**

```bash
git add library-server/src/main/java/com/library/controller/AdminReportController.java
git commit -m "feat: add GET /api/admin/reports/pending-count endpoint"
```

---

### Task 4: Frontend — admin-app api/report.js add getPendingCount

**Files:**
- Modify: `admin-app/src/api/report.js:9`

- [ ] **Step 1: Add getPendingCount function**

Add after `resolveReport`:

```js
export function getPendingCount() {
  return adminApi.get('/reports/pending-count')
}
```

- [ ] **Step 2: Verify build**

Run: `cd admin-app && npx vite build --mode development 2>&1 | tail -3`
Expected: built successfully

- [ ] **Step 3: Commit**

```bash
git add admin-app/src/api/report.js
git commit -m "feat: add getPendingCount API to admin report module"
```

---

### Task 5: Frontend — AdminHeader.vue add badge and polling

**Files:**
- Modify: `admin-app/src/components/AdminHeader.vue:1-113`

- [ ] **Step 1: Add badge to "举报管理" link in template**

Replace line 10:

```html
      <router-link class="nav-link desktop-only" to="/reports">举报管理</router-link>
```

With:

```html
      <router-link class="nav-link desktop-only" to="/reports" style="position:relative">
        举报管理
        <span v-if="pendingCount > 0" class="badge">{{ pendingCount > 99 ? '99+' : pendingCount }}</span>
      </router-link>
```

- [ ] **Step 2: Add badge to mobile menu "举报管理" link**

Replace line 20:

```html
      <router-link class="mobile-menu-item" to="/reports" @click="menuOpen = false">举报管理</router-link>
```

With:

```html
      <router-link class="mobile-menu-item" to="/reports" @click="menuOpen = false" style="position:relative">
        举报管理
        <span v-if="pendingCount > 0" class="badge-mobile">{{ pendingCount > 99 ? '99+' : pendingCount }}</span>
      </router-link>
```

- [ ] **Step 3: Add imports, state, and polling in script**

Replace the `<script setup>` block (lines 28-42):

```js
<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { getPendingCount } from '../api/report'

const menuOpen = ref(false)
const pendingCount = ref(0)
let pollingTimer = null

const username = computed(() => localStorage.getItem('username') || '管理员')

async function fetchPendingCount() {
  try {
    const data = await getPendingCount()
    pendingCount.value = data.count || 0
  } catch { /* ignore */ }
}

function logout() {
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  window.location.href = 'http://localhost:5174'
}

function toggleMenu() {
  menuOpen.value = !menuOpen.value
}

onMounted(() => {
  fetchPendingCount()
  pollingTimer = setInterval(fetchPendingCount, 30000)
})

onUnmounted(() => {
  if (pollingTimer) clearInterval(pollingTimer)
})
</script>
```

- [ ] **Step 4: Add badge CSS**

Add before the closing `</style>` tag (before line 113):

```css
.badge {
  position: absolute; top: -6px; right: -10px;
  background: var(--color-danger, #c04040); color: #fff;
  font-size: 10px; padding: 1px 5px; border-radius: 10px;
  min-width: 16px; text-align: center; line-height: 16px;
}
.badge-mobile {
  background: var(--color-danger, #c04040); color: #fff;
  font-size: 10px; padding: 1px 5px; border-radius: 10px;
  min-width: 16px; text-align: center; line-height: 16px;
  margin-left: 6px;
  vertical-align: middle;
}
```

- [ ] **Step 5: Verify build**

Run: `cd admin-app && npx vite build --mode development 2>&1 | tail -3`
Expected: built successfully

- [ ] **Step 6: Commit**

```bash
git add admin-app/src/components/AdminHeader.vue
git commit -m "feat: add pending report badge with 30s polling to admin header"
```
