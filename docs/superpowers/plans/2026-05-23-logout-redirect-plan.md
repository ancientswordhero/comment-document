# Logout Redirect to Reader App — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** After logout, redirect both readers and admins to reader-app homepage (non-logged-in state) instead of login-app.

**Architecture:** Three frontend-only changes. reader-app stays in-app via `router.push('/')`; admin-app redirects cross-origin to reader-app at localhost:5174. No backend changes — JWT remains stateless, token invalidation is client-side only (clear localStorage).

**Tech Stack:** Vue 3 + Vue Router (reader-app), Vue 3 + Vue Router (admin-app), Axios

---

### Task 1: reader-app — Change onLogout to stay in-app

**Files:**
- Modify: `reader-app/src/components/BannerHeader.vue:78-131`

- [ ] **Step 1: Import useRouter from vue-router**

In `<script setup>`, add `useRouter` to the vue import:

```js
import { ref, computed, onMounted } from 'vue'
```
→
```js
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
```

- [ ] **Step 2: Get router instance**

After the existing ref declarations (after line 91 `const profileUserId = ref(null)`), add:

```js
const router = useRouter()
```

- [ ] **Step 3: Change onLogout to use router.push('/')**

Replace the `onLogout` function body (lines 112-117):

```js
function onLogout() {
  if (!confirm('确定要退出登录吗？')) return
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  window.location.href = 'http://localhost:5176'
}
```
→
```js
function onLogout() {
  if (!confirm('确定要退出登录吗？')) return
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  router.push('/')
}
```

- [ ] **Step 4: Verify the change builds**

Run: `cd reader-app && npx vite build --mode development 2>&1 | tail -5`
Expected: No build errors.

- [ ] **Step 5: Commit**

```bash
git add reader-app/src/components/BannerHeader.vue
git commit -m "fix: logout redirects to reader-app home instead of login page"
```

---

### Task 2: admin-app — Change AdminHeader logout redirect

**Files:**
- Modify: `admin-app/src/components/AdminHeader.vue:34-38`

- [ ] **Step 1: Change redirect URL**

Replace line 37:

```js
  window.location.href = 'http://localhost:5176'
```
→
```js
  window.location.href = 'http://localhost:5174'
```

- [ ] **Step 2: Verify the change builds**

Run: `cd admin-app && npx vite build --mode development 2>&1 | tail -5`
Expected: No build errors.

- [ ] **Step 3: Commit**

```bash
git add admin-app/src/components/AdminHeader.vue
git commit -m "fix: admin logout redirects to reader-app instead of login page"
```

---

### Task 3: admin-app — Change 401/403 interceptor redirect

**Files:**
- Modify: `admin-app/src/api/index.js:31-34`

- [ ] **Step 1: Change redirect URL**

Replace line 33:

```js
        window.location.href = 'http://localhost:5176'
```
→
```js
        window.location.href = 'http://localhost:5174'
```

- [ ] **Step 2: Verify the change builds**

Run: `cd admin-app && npx vite build --mode development 2>&1 | tail -5`
Expected: No build errors.

- [ ] **Step 3: Commit**

```bash
git add admin-app/src/api/index.js
git commit -m "fix: 401/403 interceptor redirects to reader-app instead of login page"
```
