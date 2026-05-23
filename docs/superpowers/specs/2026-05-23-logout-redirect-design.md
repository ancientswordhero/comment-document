# Logout Redirect to Reader App

**Date:** 2026-05-23

## Summary

After logging out, both readers and admins should land on the reader-app homepage (non-logged-in state) instead of the login page.

## Current Behavior

| App | Logout Action | Dest |
|------|-------------|------|
| reader-app | BannerHeader onLogout | → localhost:5176 (login-app) |
| admin-app | AdminHeader logout | → localhost:5176 (login-app) |
| admin-app | 401/403 interceptor | → localhost:5176 (login-app) |

## Target Behavior

| App | Logout Action | Dest |
|------|-------------|------|
| reader-app | BannerHeader onLogout | → localhost:5174 `/` (reader-app home) |
| admin-app | AdminHeader logout | → localhost:5174 (reader-app) |
| admin-app | 401/403 interceptor | → localhost:5174 (reader-app) |

## Changes

### 1. `reader-app/src/components/BannerHeader.vue` — onLogout

Replace `window.location.href = 'http://localhost:5176'` with `router.push('/')`.

The reader is already on reader-app; clearing token and navigating home suffices. The header recomputes `isLoggedIn` and shows the logged-out nav.

### 2. `admin-app/src/components/AdminHeader.vue` — logout

Change redirect URL from `http://localhost:5176` to `http://localhost:5174`.

### 3. `admin-app/src/api/index.js` — response interceptor

Change 401/403 redirect URL from `http://localhost:5176` to `http://localhost:5174`.

## Unchanged

- Login flow (login-app at 5176 remains the auth entry point)
- Router guards that redirect unauthenticated users to 5176 for login
- "Login required" prompts in reader-app (point to 5176)
- Backend: no changes needed (JWT remains stateless, no logout endpoint)
