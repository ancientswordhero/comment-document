# Bilibili 视差 Header 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 改造 BannerHeader.vue，实现 B 站风格鼠标驱动横向视差背景效果，使用用户提供的 32 张 3840×360 webp 横幅素材。

**Architecture:** 单文件改动 (BannerHeader.vue)，mousemove 驱动 requestAnimationFrame + lerp 缓动控制背景图 translateX。移动端降级为静态展示。导航栏、搜索框、用户操作逻辑全部保留不变。

**Tech Stack:** Vue 3 (Composition API), Vite (import.meta.glob), CSS transform

---

### Task 1: 复制 webp 素材到项目目录

**Files:**
- Create: `reader-app/src/assets/header/*.webp` (32 files)

- [ ] **Step 1: 将 32 张 webp 文件从桌面复制到项目中**

```bash
mkdir -p reader-app/src/assets/header
cp /c/Users/36295/Desktop/header/*.webp reader-app/src/assets/header/
```

- [ ] **Step 2: 验证文件已到位**

```bash
ls reader-app/src/assets/header/*.webp | wc -l
```
Expected: `32`

- [ ] **Step 3: 提交素材文件**

```bash
git add reader-app/src/assets/header/
git commit -m "feat: add 32 header parallax webp images"
```

---

### Task 2: 重写 BannerHeader.vue 视差逻辑

**Files:**
- Modify: `reader-app/src/components/BannerHeader.vue`

- [ ] **Step 1: 重写模板 — 移除角色装饰，保留背景图作为视差层**

将 `<template>` 中的 banner 区域改为：

```html
<template>
  <div class="banner-wrapper">
    <!-- 导航栏 -->
    <div class="banner-nav">
      <div class="nav-left">
        <span class="nav-logo-icon">書</span>
        <span class="nav-logo-text">云图书馆</span>
      </div>
      <div class="nav-links">
        <span class="nav-item active">首页</span>
        <span class="nav-sep">|</span>
        <span class="nav-item">分类浏览</span>
        <span class="nav-sep">|</span>
        <span class="nav-item">最新上架</span>
      </div>
      <div class="nav-right">
        <template v-if="isLoggedIn">
          <span class="nav-item" @click="$router.push('/bookshelf')">我的书架</span>
          <span class="nav-sep">|</span>
          <span class="nav-item" @click="$router.push('/inbox')" style="position:relative">
            收件箱
            <span v-if="unreadCount > 0" class="badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
          </span>
          <span class="nav-sep">|</span>
          <span class="nav-user" @click="onViewSelf" title="查看个人主页">{{ username }}</span>
          <span class="nav-sep">|</span>
          <span class="nav-item logout" @click="onLogout">退出</span>
          <span class="nav-sep">|</span>
          <span class="nav-item danger" @click="onDeleteAccount">注销账号</span>
        </template>
        <a v-else href="http://localhost:5176" class="nav-admin">读者/管理登录</a>
      </div>
    </div>

    <!-- 横幅 -->
    <div class="banner" ref="bannerRef" @mousemove="onMouseMove" @mouseleave="onMouseLeave">
      <!-- 背景图（视差层） -->
      <div class="banner-bg-wrap" ref="bgWrapRef">
        <img :src="currentBg" class="banner-bg" alt="" />
      </div>

      <!-- 渐变遮罩 -->
      <div class="banner-mask"></div>

      <!-- 中央内容 -->
      <div class="banner-center">
        <div class="banner-logo">
          <span class="logo-icon">書</span>
          <span class="logo-text">云图书馆</span>
        </div>
        <div class="banner-search">
          <input
            v-model="searchKeywords"
            class="search-input"
            placeholder="搜索书名 · 作者 · ISBN"
            @keyup.enter="onSearch"
          />
          <button type="button" class="search-btn" @click="onSearch">搜索</button>
        </div>
        <div class="banner-tagline">万卷古今消永日 · 一窗昏晓送流年</div>
      </div>
    </div>

    <UserProfileDialog
      v-if="profileUserId"
      :user-id="profileUserId"
      :visible="showProfile"
      @close="showProfile = false"
    />
  </div>
</template>
```

- [ ] **Step 2: 重写 script — 添加视差逻辑**

```js
<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { setSearchKeyword } from '../composables/useSearch'
import { getUserIdFromToken } from '../utils/jwt'
import { deleteAccount } from '../api/auth'
import { getUnreadCount } from '../api/report'
import UserProfileDialog from './UserProfileDialog.vue'

const searchKeywords = ref('')
const unreadCount = ref(0)
const showProfile = ref(false)
const profileUserId = ref(null)

const bannerRef = ref(null)
const bgWrapRef = ref(null)

// ========== 图片管理 ==========
const headerModules = import.meta.glob('../assets/header/*.webp', { eager: true, import: 'default' })
const headerUrls = Object.values(headerModules)  // string[]
const currentBg = ref('')

function pickRandomBg() {
  const i = Math.floor(Math.random() * headerUrls.length)
  currentBg.value = headerUrls[i]
}

// ========== 视差逻辑 ==========
const MAX_OFFSET = 60       // px
const LERP_SPEED = 0.08
const isTouchDevice = 'ontouchstart' in window || navigator.maxTouchPoints > 0

let targetX = 0
let currentX = 0
let rafId = null

function lerp() {
  currentX += (targetX - currentX) * LERP_SPEED
  if (Math.abs(targetX - currentX) < 0.1) {
    currentX = targetX
  }
  if (bgWrapRef.value) {
    bgWrapRef.value.style.transform = `translateX(${currentX}px)`
  }
  if (currentX !== targetX) {
    rafId = requestAnimationFrame(lerp)
  }
}

function startLerp() {
  if (rafId) return
  rafId = requestAnimationFrame(lerp)
}

function onMouseMove(e) {
  if (isTouchDevice) return
  const rect = bannerRef.value?.getBoundingClientRect()
  if (!rect) return
  const ratio = (e.clientX - rect.left) / rect.width          // 0~1
  targetX = (ratio - 0.5) * 2 * MAX_OFFSET                    // -MAX_OFFSET ~ +MAX_OFFSET
  startLerp()
}

function onMouseLeave() {
  targetX = 0
  startLerp()
}

// ========== 业务逻辑（不变） ==========
const isLoggedIn = computed(() => !!localStorage.getItem('token'))
const username = computed(() => localStorage.getItem('username') || '读者')
const currentUserId = computed(() => {
  const token = localStorage.getItem('token')
  if (!token) return null
  try { return getUserIdFromToken(token) } catch { return null }
})

function onViewSelf() {
  if (!currentUserId.value) return
  profileUserId.value = currentUserId.value
  showProfile.value = true
}

function onSearch() {
  const keyword = searchKeywords.value.trim()
  setSearchKeyword(keyword)
}

function onLogout() {
  if (!confirm('确定要退出登录吗？')) return
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  window.location.href = '/'
}

async function onDeleteAccount() {
  if (!confirm('确定要注销账号吗？此操作不可撤销，您的所有数据将被清除。')) return
  try {
    await deleteAccount()
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    window.location.href = 'http://localhost:5176'
  } catch (e) {
    const msg = e?.response?.data?.message || e?.message || '注销失败，请重试'
    alert(msg)
  }
}

onMounted(async () => {
  pickRandomBg()
  if (isLoggedIn.value) {
    try { unreadCount.value = await getUnreadCount() } catch { /* ignore */ }
  }
})

onBeforeUnmount(() => {
  if (rafId) cancelAnimationFrame(rafId)
})
</script>
```

- [ ] **Step 3: 重写 style — 背景图使用 transform 定位，移除角色样式**

将 `<style scoped>` 中的 banner 相关样式改为：

```css
<style scoped>
/* ============================================
   BannerHeader - Bilibili 视差风格
   ============================================ */

.banner-wrapper {
  position: relative;
  height: 420px;
}

/* ---- 导航栏 ---- */
.banner-nav {
  position: absolute;
  top: 0; left: 0; right: 0;
  z-index: 20;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 36px;
  background: rgba(0,0,0,0.10);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
}
.nav-left {
  display: flex; align-items: center; gap: 8px;
  flex: 1;
}
.nav-right {
  display: flex; align-items: center; gap: 4px;
  flex: 1;
  justify-content: flex-end;
}
.nav-logo-icon {
  width: 26px; height: 26px;
  background: var(--color-primary, #c9a96e);
  color: #fff;
  display: flex; align-items: center; justify-content: center;
  font-size: 13px; border-radius: 5px;
}
.nav-logo-text {
  font-size: 14px; font-weight: 600; color: #fff;
  font-family: var(--font-serif); letter-spacing: 2px;
}
.nav-links {
  display: flex; align-items: center;
}
.nav-item {
  font-size: 13px; color: rgba(255,255,255,0.7);
  cursor: pointer; transition: color 0.2s; padding: 4px 10px;
}
.nav-item:hover,
.nav-item.active { color: #fff; }
.nav-sep {
  margin: 0 6px; color: rgba(255,255,255,0.15); font-size: 12px;
}
.nav-admin {
  padding: 5px 18px;
  background: rgba(255,255,255,0.12);
  color: #fff; border-radius: 16px;
  font-size: 12px; text-decoration: none;
  border: 1px solid rgba(255,255,255,0.15);
  transition: all 0.2s;
}
.nav-admin:hover {
  background: var(--color-primary, #c9a96e);
  border-color: transparent;
}
.nav-user {
  font-size: 12px; color: rgba(255,255,255,0.8);
  font-family: var(--font-serif);
  cursor: pointer;
  transition: color 0.2s;
}
.nav-user:hover { color: var(--color-primary, #c9a96e); }
.nav-item.logout {
  font-size: 12px; color: rgba(255,255,255,0.6);
  cursor: pointer; transition: color 0.2s;
}
.nav-item.logout:hover { color: #ff6b6b; }
.nav-item.danger { font-size: 12px; color: rgba(255,255,255,0.5); cursor: pointer; transition: color 0.2s; }
.nav-item.danger:hover { color: #ff6b6b; }

/* ---- 横幅主体 ---- */
.banner {
  position: absolute;
  inset: 0;
  overflow: hidden;
}

/* 背景图：视差层 */
.banner-bg-wrap {
  position: absolute;
  inset: -60px 0 0 0;       /* 上下扩展以防 translateX 露出白边 */
  width: calc(100% + 120px);
  left: -60px;
  will-change: transform;
}
.banner-bg {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center;
}

/* 渐变遮罩：顶部稍暗 -> 底部融入页面背景 */
.banner-mask {
  position: absolute;
  inset: 0;
  z-index: 1;
  background: linear-gradient(
    180deg,
    rgba(0,0,0,0.20) 0%,
    transparent 35%,
    transparent 70%,
    var(--color-bg, #fafaf7) 100%
  );
  pointer-events: none;
}

/* 中央内容 */
.banner-center {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  z-index: 3;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 10px;
}
.banner-logo {
  display: flex; align-items: center; gap: 12px; margin-bottom: 22px;
}
.logo-icon {
  width: 46px; height: 46px;
  background: var(--color-primary, #c9a96e);
  color: #fff;
  display: flex; align-items: center; justify-content: center;
  font-size: 23px; border-radius: 8px;
  box-shadow: 0 2px 16px rgba(0,0,0,0.15);
}
.logo-text {
  font-weight: 700; font-size: 30px; color: #fff;
  font-family: var(--font-serif); letter-spacing: 6px;
  text-shadow: 0 2px 10px rgba(0,0,0,0.2);
}
.banner-search {
  display: flex; align-items: center;
  background: #fff; border-radius: 28px;
  box-shadow: 0 4px 24px rgba(0,0,0,0.10);
  overflow: hidden;
  width: 500px; max-width: 78vw;
}
.banner-search:focus-within {
  box-shadow: 0 4px 28px rgba(0,0,0,0.18);
}
.search-input {
  flex: 1; border: none; padding: 14px 22px;
  font-size: 14px; color: var(--color-text, #4a3d2f);
  outline: none; background: transparent;
}
.search-input::placeholder { color: var(--color-text-muted, #a09880); }
.search-btn {
  padding: 14px 28px;
  background: var(--color-primary, #c9a96e);
  color: #fff; border: none;
  font-size: 14px; cursor: pointer;
  font-family: var(--font-serif); letter-spacing: 2px;
  transition: background 0.2s;
}
.search-btn:hover { background: var(--color-primary-hover, #b8944d); }
.banner-tagline {
  margin-top: 16px;
  font-size: 13px;
  color: #fff;
  letter-spacing: 4px;
  text-shadow: 0 1px 6px rgba(0,0,0,0.4), 0 0 12px rgba(0,0,0,0.2);
}

/* ---- 响应式 ---- */
@media (max-width: 768px) {
  .banner-wrapper { height: 280px; }
  .banner-nav { padding: 6px 16px; }
  .nav-links { display: none; }
  .logo-text { font-size: 20px; letter-spacing: 3px; }
  .logo-icon { width: 34px; height: 34px; font-size: 17px; }
  .banner-search { width: 85vw; }
  .search-input { padding: 10px 14px; font-size: 13px; }
  .search-btn { padding: 10px 18px; font-size: 12px; }
  .banner-bg-wrap {
    inset: 0;
    width: 100%;
    left: 0;
  }
}
@media (max-width: 480px) {
  .banner-wrapper { height: 200px; }
  .banner-logo { margin-bottom: 12px; }
  .logo-text { font-size: 17px; letter-spacing: 2px; }
  .logo-icon { width: 28px; height: 28px; font-size: 14px; }
  .banner-search { width: 90vw; }
  .search-input { padding: 9px 12px; font-size: 12px; }
  .search-btn { padding: 9px 14px; font-size: 11px; }
  .banner-tagline { display: none; }
  .banner-nav { padding: 6px 12px; }
  .nav-logo-text { font-size: 11px; }
}
.badge {
  position: absolute; top: -6px; right: -10px;
  background: var(--color-danger, #c04040); color: #fff;
  font-size: 10px; padding: 1px 5px; border-radius: 10px;
  min-width: 16px; text-align: center; line-height: 16px;
}
</style>
```

关键变化：
- 移除 `banner-character-wrap` / `banner-character` 样式
- `.banner-bg-wrap` 使用 `will-change: transform`，宽高扩展 120px 防止平移时露出白边
- 响应式断点移除 `1024px`（因为不再需要角色缩放），在 `768px` 以下恢复静态布局（`width: 100%`, `left: 0`）
- 移动端对视差的禁用由 JS 中 `isTouchDevice` 处理

- [ ] **Step 4: 提交 BannerHeader.vue 改动**

```bash
git add reader-app/src/components/BannerHeader.vue
git commit -m "feat: implement bilibili parallax header with mouse-driven translateX"
```

---

### Task 3: 手动验证

- [ ] **Step 1: 启动 reader-app 开发服务器**

```bash
cd reader-app && npx vite --port 5173 &
```

- [ ] **Step 2: 验证桌面端视差效果**
  - 浏览器打开 `http://localhost:5173`
  - 鼠标在 banner 区域左右移动，确认背景图平滑跟随
  - 鼠标移出 banner，确认背景缓慢回到居中
  - 检查搜索框、导航链接、登录按钮正常

- [ ] **Step 3: 验证移动端静态降级**
  - Chrome DevTools → Toggle device toolbar → 选移动设备
  - 刷新页面，确认背景静态居中、无抖动
  - 确认导航栏和搜索框可正常使用

- [ ] **Step 4: 停止开发服务器**
