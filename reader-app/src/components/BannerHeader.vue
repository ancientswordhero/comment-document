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
          <span class="nav-user">{{ username }}</span>
          <span class="nav-sep">|</span>
          <span class="nav-item logout" @click="onLogout">退出</span>
        </template>
        <a v-else href="http://localhost:5176" class="nav-admin">读者/管理登录</a>
      </div>
    </div>

    <!-- 横幅 -->
    <div class="banner">
      <!-- 背景图 -->
      <div class="banner-bg-wrap">
        <img :src="bannerBg" class="banner-bg" alt="" />
      </div>

      <!-- 渐变遮罩 -->
      <div class="banner-mask"></div>

      <!-- 左侧角色 -->
      <div class="banner-character-wrap">
        <img :src="characterSrc" class="banner-character" alt="" />
      </div>

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
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { setSearchKeyword } from '../composables/useSearch'
import bannerBg from '../assets/banner-bg.jpg'
import characterSrc from '../assets/banner-character.png'

const searchKeywords = ref('')

const isLoggedIn = computed(() => !!localStorage.getItem('token'))
const username = computed(() => localStorage.getItem('username') || '读者')

function onSearch() {
  const keyword = searchKeywords.value.trim()
  setSearchKeyword(keyword)
}

function onLogout() {
  if (!confirm('确定要注销登录吗？')) return
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  window.location.href = 'http://localhost:5174'
}
</script>

<style scoped>
/* ============================================
   BannerHeader - 哔哩哔哩风格
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
}
.nav-item.logout {
  font-size: 12px; color: rgba(255,255,255,0.6);
  cursor: pointer; transition: color 0.2s;
}
.nav-item.logout:hover { color: #ff6b6b; }

/* ---- 横幅主体 ---- */
.banner {
  position: absolute;
  inset: 0;
  overflow: hidden;
}

/* 背景图 */
.banner-bg-wrap {
  position: absolute;
  inset: 0;
}
.banner-bg {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center;
}

/* 渐变遮罩：顶部稍暗 -> 中间透明 -> 底部融入页面背景 */
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

/* 角色：左侧绝对定位 */
.banner-character-wrap {
  position: absolute;
  left: 0;
  bottom: 0;
  z-index: 2;
  pointer-events: none;
}
.banner-character {
  display: block;
  height: 400px;
  width: auto;
  filter: drop-shadow(0 0 20px rgba(0,0,0,0.12));
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
@media (max-width: 1024px) {
  .banner-character { height: 320px; }
}
@media (max-width: 768px) {
  .banner-wrapper { height: 280px; }
  .banner-character { height: 220px; opacity: 0.5; }
  .banner-nav { padding: 6px 16px; }
  .nav-links { display: none; }
  .logo-text { font-size: 20px; letter-spacing: 3px; }
  .logo-icon { width: 34px; height: 34px; font-size: 17px; }
  .banner-search { width: 85vw; }
  .search-input { padding: 10px 14px; font-size: 13px; }
  .search-btn { padding: 10px 18px; font-size: 12px; }
}
@media (max-width: 480px) {
  .banner-wrapper { height: 200px; }
  .banner-character { display: none; }
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
</style>
