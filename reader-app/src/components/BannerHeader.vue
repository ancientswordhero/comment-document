<template>
  <div class="banner-wrapper">
    <!-- 导航栏：叠加在视差横幅上 -->
    <div class="banner-nav">
      <div class="nav-left" @click="goDiscover" title="书海墨韵">
        <span class="nav-logo-icon">書</span>
        <span class="nav-logo-text">云图书馆</span>
      </div>
      <div class="nav-links">
        <span class="nav-item" :class="{ active: $route.path === '/' }" @click="$router.push('/')">首页</span>
        <span class="nav-sep">|</span>
        <span class="nav-item" :class="{ active: $route.path === '/notes' }" @click="$router.push('/notes')">书余</span>
        <span class="nav-sep">|</span>
        <span class="nav-item" :class="{ active: $route.path === '/guide' }" @click="$router.push('/guide')">凡例</span>
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

    <!-- 视差横幅：纯秋天场景 -->
    <div
      class="fall-banner"
      @mouseenter="onBannerEnter"
      @mousemove="onBannerMove"
      @mouseleave="onBannerLeave"
    >
      <div ref="animateBannerRef" class="animate-banner">
        <div class="layer">
          <img
            src="/images/autumn/background.png"
            data-width="3000"
            data-height="250"
            alt="background"
          />
        </div>
        <div class="layer">
          <img
            :src="girlImgSrc"
            data-width="3000"
            data-height="275"
            alt="girl"
          />
        </div>
        <div class="layer">
          <img
            src="/images/autumn/hill.png"
            data-width="3000"
            data-height="250"
            alt="hill"
          />
        </div>
        <div class="layer">
          <img
            src="/images/autumn/foreground.png"
            data-width="3000"
            data-height="250"
            alt="foreground"
          />
        </div>
        <div class="layer">
          <img
            src="/images/autumn/fairy.png"
            data-width="3000"
            data-height="275"
            alt="fairy"
          />
        </div>
        <div class="layer">
          <img
            src="/images/autumn/leaf.png"
            data-width="3000"
            data-height="275"
            alt="leaf"
          />
        </div>
      </div>

      <!-- 渐变遮罩：底部融入页面背景 -->
      <div class="banner-mask"></div>
    </div>

    <UserProfileDialog
      v-if="profileUserId"
      :user-id="profileUserId"
      :visible="showProfile"
      @close="showProfile = false"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { getUserIdFromToken } from '../utils/jwt'
import { deleteAccount } from '../api/auth'
import { useUnreadBadge } from '../composables/useUnreadBadge'
import UserProfileDialog from './UserProfileDialog.vue'

const { unreadCount, fetchUnreadCount } = useUnreadBadge()
const showProfile = ref(false)
const profileUserId = ref(null)

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

function goDiscover() {
  window.location.href = '/scene/index.html'
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

/* ============================================
   视差 Banner（秋天多层视差 — bilibili 风格）
   ============================================ */

const animateBannerRef = ref(null)

// 女孩眨眼
const eyeOpen = '/images/autumn/girl-eye-open.png'
const eyeNapping = '/images/autumn/girl-eye-napping.png'
const eyeClosed = '/images/autumn/girl-eye-closed.png'
const girlImgSrc = ref(eyeOpen)

// 各层视差配置
const initConfig = [
  { aspect: 1,    blur: 4, x: 0,   y: 0,    blurEffect: (b, p) => b + p * b,        parallaxX: (x) => x },
  { aspect: 0.6,  blur: 0, x: 0,   y: 0,    blurEffect: (b, p) => Math.abs(p * 10),  parallaxX: (x, p) => x - p * 10 },
  { aspect: 1,    blur: 1, x: -50, y: 0,    blurEffect: (b, p) => Math.abs(b - p * 4),  parallaxX: (x, p) => x - p * 30 },
  { aspect: 0.6,  blur: 4, x: 0,   y: 4.2,  blurEffect: (b, p) => Math.abs(b - p * 8),  parallaxX: (x, p) => x - p * 45 },
  { aspect: 0.6,  blur: 5, x: 0,   y: -1.8, blurEffect: (b, p) => Math.abs(b - p * 8),  parallaxX: (x, p) => x - p * 95 },
  { aspect: 0.65, blur: 6, x: 0,   y: 0,    blurEffect: (b, p) => Math.abs(b - p * 4),  parallaxX: (x, p) => x - p * 118 },
]

const breakpoint = 1658
let endpoint = { width: 0, x: 0 }
let blinkTimer = null
let resizeObserver = null

function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms))
}

async function makeBlink() {
  await sleep(50)
  girlImgSrc.value = eyeNapping
  await sleep(50)
  girlImgSrc.value = eyeClosed
  await sleep(350)
  girlImgSrc.value = eyeOpen
  blinkTimer = setTimeout(makeBlink, 5000)
}

function movementTemplate(blur, x, y) {
  return `filter: blur(${blur}px); transform: translate(${x}px, ${y}px) translateZ(0);`
}

function getInitStyle(key) {
  const cfg = initConfig[key]
  return movementTemplate(cfg.blur, cfg.x, cfg.y)
}

function initRects() {
  const banner = animateBannerRef.value
  if (!banner) return
  const bannerWidth = banner.getBoundingClientRect().width
  const imgs = banner.querySelectorAll('.layer img')

  imgs.forEach((img, key) => {
    const cfg = initConfig[key]
    const originWidth = parseInt(img.dataset.width, 10)
    const originHeight = parseInt(img.dataset.height, 10)

    let width, height
    if (bannerWidth < breakpoint) {
      width = cfg.aspect * originWidth
      height = cfg.aspect * originHeight
    } else {
      const extra = Math.floor((bannerWidth - breakpoint) / 10)
      width = cfg.aspect * originWidth + extra * 12
      height = cfg.aspect * originHeight + extra * 1
    }

    img.width = width
    img.height = height
    img.style.cssText = getInitStyle(key)
  })
}

function applyEffect(imgs, parallax) {
  imgs.forEach((img, key) => {
    const cfg = initConfig[key]
    const blur = cfg.blurEffect(cfg.blur, parallax)
    const x = cfg.parallaxX(cfg.x, parallax)
    img.style.cssText = movementTemplate(blur, x, cfg.y)
  })
}

function resetEffect() {
  endpoint = { width: 0, x: 0 }
  const banner = animateBannerRef.value
  if (!banner) return
  const imgs = banner.querySelectorAll('.layer img')
  imgs.forEach((img, key) => {
    img.style.cssText = `transition-duration: 0.2s; ${getInitStyle(key)}`
  })
}

function getImgs() {
  const banner = animateBannerRef.value
  return banner ? banner.querySelectorAll('.layer img') : []
}

function onBannerEnter(e) {
  const banner = animateBannerRef.value
  if (!banner) return
  const { width } = banner.getBoundingClientRect()
  endpoint.x = e.clientX
  endpoint.width = width
}

function onBannerMove(e) {
  if (endpoint.width === 0) return
  const parallax = e.clientX - endpoint.x
  const parallaxRatio = parallax / endpoint.width
  applyEffect(getImgs(), parallaxRatio)
}

function onBannerLeave() {
  resetEffect()
}

onMounted(async () => {
  if (isLoggedIn.value) {
    try { await fetchUnreadCount() } catch { /* ignore */ }
  }

  await nextTick()
  initRects()
  makeBlink()

  if (animateBannerRef.value) {
    resizeObserver = new ResizeObserver(() => {
      initRects()
    })
    resizeObserver.observe(animateBannerRef.value)
  }
})

onBeforeUnmount(() => {
  if (blinkTimer) clearTimeout(blinkTimer)
  if (resizeObserver) resizeObserver.disconnect()
})
</script>

<style scoped>
/* ============================================
   BannerHeader — 秋天视差头部
   比例：9.375vw / min 155px（B 站规格）
   ============================================ */

.banner-wrapper {
  position: relative;
  z-index: 0;
  min-height: 155px;
  height: 9.375vw;
  min-width: 999px;
  background-color: #f9f9f9;
}

/* ---- 导航栏 ---- */
.banner-nav {
  position: absolute;
  top: 0; left: 0; right: 0;
  z-index: 20;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 36px;
  background: linear-gradient(
    180deg,
    rgba(0,0,0,0.12) 0%,
    rgba(0,0,0,0.04) 60%,
    transparent 100%
  );
}
.nav-left {
  display: flex; align-items: center; gap: 8px;
  flex: 1;
  cursor: pointer;
}
.nav-right {
  display: flex; align-items: center; gap: 4px;
  flex: 1;
  justify-content: flex-end;
}
.nav-logo-icon {
  width: 24px; height: 24px;
  background: var(--color-primary, #c9a96e);
  color: #fff;
  display: flex; align-items: center; justify-content: center;
  font-size: 12px; border-radius: 4px;
}
.nav-logo-text {
  font-size: 13px; font-weight: 600; color: #fff;
  font-family: var(--font-serif); letter-spacing: 2px;
  text-shadow: 0 1px 3px rgba(0,0,0,0.2);
}
.nav-links {
  display: flex; align-items: center;
}
.nav-item {
  font-size: 12px; color: rgba(255,255,255,0.75);
  cursor: pointer; transition: color 0.2s; padding: 3px 8px;
}
.nav-item:hover,
.nav-item.active { color: #fff; }
.nav-sep {
  margin: 0 4px; color: rgba(255,255,255,0.15); font-size: 11px;
}
.nav-admin {
  padding: 4px 16px;
  background: rgba(255,255,255,0.12);
  color: #fff; border-radius: 14px;
  font-size: 11px; text-decoration: none;
  border: 1px solid rgba(255,255,255,0.15);
  transition: all 0.2s;
}
.nav-admin:hover {
  background: var(--color-primary, #c9a96e);
  border-color: transparent;
}
.nav-user {
  font-size: 11px; color: rgba(255,255,255,0.8);
  font-family: var(--font-serif);
  cursor: pointer;
  transition: color 0.2s;
}
.nav-user:hover { color: var(--color-primary, #c9a96e); }
.nav-item.logout {
  font-size: 11px; color: rgba(255,255,255,0.6);
  cursor: pointer; transition: color 0.2s;
}
.nav-item.logout:hover { color: #ff6b6b; }
.nav-item.danger { font-size: 11px; color: rgba(255,255,255,0.5); cursor: pointer; transition: color 0.2s; }
.nav-item.danger:hover { color: #ff6b6b; }

/* ---- 视差横幅 ---- */
.fall-banner {
  position: absolute;
  inset: 0;
  overflow: hidden;
  display: flex;
  justify-content: center;
}

.animate-banner {
  position: absolute;
  inset: 0;
  overflow: hidden;
}

.layer {
  position: absolute;
  inset: 0;
  height: 100%;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.layer img {
  max-width: none;
  perspective: 1000;
}

/* 渐变遮罩：底部自然融入页面 */
.banner-mask {
  position: absolute;
  inset: 0;
  z-index: 1;
  background: linear-gradient(
    180deg,
    transparent 0%,
    transparent 60%,
    var(--color-bg, #fafaf7) 100%
  );
  pointer-events: none;
}

/* ---- 响应式 ---- */
@media (max-width: 768px) {
  .banner-nav { padding: 6px 16px; }
  .nav-links { display: none; }
}
@media (max-width: 480px) {
  .banner-nav { padding: 4px 12px; }
  .nav-logo-text { font-size: 11px; }
}

.badge {
  position: absolute; top: -6px; right: -10px;
  background: var(--color-danger, #c04040); color: #fff;
  font-size: 10px; padding: 1px 5px; border-radius: 10px;
  min-width: 16px; text-align: center; line-height: 16px;
}
</style>
