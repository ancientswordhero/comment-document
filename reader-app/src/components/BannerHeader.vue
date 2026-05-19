<template>
  <div class="banner-wrapper">
    <div class="banner">
      <img :src="bannerBg" class="banner-bg" alt="" />
      <div class="banner-overlay"></div>
      <img :src="chibiSrc" class="banner-chibi" alt="" />
      <img :src="characterSrc" class="banner-character" alt="" />
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
          <button class="search-btn" @click="onSearch">搜索</button>
        </div>
        <div class="banner-tagline">万卷古今消永日 · 一窗昏晓送流年</div>
      </div>
    </div>
    <div class="banner-nav">
      <span class="nav-item active">首页</span>
      <span class="nav-sep">|</span>
      <span class="nav-item">分类浏览</span>
      <span class="nav-sep">|</span>
      <span class="nav-item">最新上架</span>
      <a href="http://localhost:5174" class="nav-admin">管理后台</a>
    </div>
  </div>
</template>

<script setup>
import { ref, onUnmounted } from 'vue'
import { setSearchKeyword } from '../composables/useSearch'
import bannerBg from '../assets/banner-bg.jpg'
import chibiSrc from '../assets/banner-chibi.png'
import characterSrc from '../assets/banner-character.png'

const searchKeywords = ref('')
let bannerTimer = null

function onSearch() {
  setSearchKeyword(searchKeywords.value)
}

onUnmounted(() => {
  if (bannerTimer) clearInterval(bannerTimer)
})
</script>

<style scoped>
.banner-wrapper { position: relative; }
.banner {
  position: relative;
  height: 300px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}
.banner-bg {
  position: absolute; inset: 0;
  width: 100%; height: 100%; object-fit: cover;
}
.banner-overlay {
  position: absolute; inset: 0;
  background: linear-gradient(180deg,
    rgba(255,255,255,0.1) 0%,
    rgba(255,255,255,0.05) 50%,
    var(--color-bg, #fafaf7) 100%
  );
}
.banner-chibi {
  position: absolute;
  bottom: 0; left: 8%;
  height: 200px; object-fit: contain; z-index: 2;
}
.banner-character {
  position: absolute;
  bottom: 0; right: 5%;
  height: 270px; object-fit: contain; z-index: 2;
}
.banner-center {
  position: relative; z-index: 3;
  display: flex; flex-direction: column; align-items: center;
}
.banner-logo { display: flex; align-items: center; gap: 10px; margin-bottom: 20px; }
.logo-icon {
  width: 40px; height: 40px;
  background: var(--color-primary, #c9a96e); color: #fff;
  display: flex; align-items: center; justify-content: center;
  font-size: 20px; border-radius: var(--radius-lg, 8px);
  box-shadow: var(--shadow-md, 0 4px 16px rgba(74,61,47,0.1));
}
.logo-text {
  font-weight: 700; font-size: 26px;
  color: var(--color-text, #4a3d2f);
  font-family: var(--font-serif, 'Noto Serif SC', serif);
  letter-spacing: 4px;
  text-shadow: 0 1px 2px rgba(255,255,255,0.8);
}
.banner-search {
  display: flex; align-items: center;
  background: #fff; border-radius: 24px;
  box-shadow: var(--shadow-md, 0 4px 16px rgba(74,61,47,0.1));
  overflow: hidden;
  width: 460px; max-width: 85vw;
}
.search-input {
  flex: 1; border: none; padding: 13px 18px;
  font-size: 13px; color: var(--color-text, #4a3d2f);
  outline: none; background: transparent;
}
.search-input::placeholder { color: var(--color-text-muted, #a09880); }
.search-btn {
  padding: 13px 24px;
  background: var(--color-primary, #c9a96e); color: #fff;
  border: none; font-size: 13px; cursor: pointer;
  font-family: var(--font-serif, 'Noto Serif SC', serif);
  letter-spacing: 2px; transition: background 0.2s;
}
.search-btn:hover { background: var(--color-primary-hover, #b8944d); }
.banner-tagline {
  margin-top: 12px; font-size: 12px;
  color: var(--color-text-secondary, #8b8070); letter-spacing: 2px;
}
.banner-nav {
  position: absolute; top: 0; left: 0; right: 0; z-index: 10;
  display: flex; align-items: center; padding: 10px 32px;
  background: rgba(255,255,255,0.12);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
}
.nav-item {
  font-size: 12px; color: var(--color-text, #4a3d2f);
  cursor: pointer; transition: color 0.2s;
}
.nav-item:hover { color: var(--color-primary, #c9a96e); }
.nav-item.active { font-weight: 500; color: var(--color-primary, #c9a96e); }
.nav-sep { margin: 0 14px; color: rgba(74,61,47,0.15); font-size: 12px; }
.nav-admin {
  margin-left: auto; padding: 5px 14px;
  background: rgba(201,169,110,0.2); color: var(--color-primary, #c9a96e);
  border-radius: 16px; font-size: 11px; text-decoration: none;
  transition: all 0.2s;
}
.nav-admin:hover { background: var(--color-primary, #c9a96e); color: #fff; }

@media (max-width: 768px) {
  .banner { height: 220px; }
  .banner-chibi { display: none; }
  .banner-character { height: 180px; right: 2%; opacity: 0.6; }
  .logo-text { font-size: 20px; }
  .logo-icon { width: 32px; height: 32px; font-size: 16px; }
  .banner-search { width: 90vw; }
  .search-input { padding: 10px 14px; font-size: 12px; }
  .search-btn { padding: 10px 18px; font-size: 12px; }
  .banner-nav { padding: 8px 16px; }
  .nav-sep { margin: 0 8px; }
}
@media (max-width: 480px) {
  .banner { height: 180px; }
  .banner-character { display: none; }
  .logo-text { font-size: 17px; letter-spacing: 2px; }
  .banner-tagline { display: none; }
  .banner-nav { padding: 6px 12px; }
  .nav-item { font-size: 11px; }
}
</style>
