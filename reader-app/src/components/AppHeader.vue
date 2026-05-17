<template>
  <header class="app-header">
    <div class="header-left">
      <span class="logo-icon">書</span>
      <span class="logo-text">云图书馆</span>
    </div>
    <div class="header-center">
      <div class="search-box">
        <span class="search-icon">🔍</span>
        <span class="search-placeholder">搜索书名 · 作者 · ISBN</span>
      </div>
    </div>
    <div class="header-right">
      <span class="nav-link">登录</span>
      <span class="nav-divider">|</span>
      <span class="nav-link">注册</span>
    </div>
  </header>
</template>

<script setup>
</script>

<style scoped>
.app-header {
  display: flex; align-items: center;
  padding: 12px 36px; gap: 28px;
  background: var(--header-bg);
  border-bottom: 1px solid var(--border);
  box-shadow: 0 1px 4px rgba(0,0,0,0.03);
  position: sticky; top: 0; z-index: 100;
}
.header-left { display: flex; align-items: center; gap: 8px; }
.logo-icon {
  width: 30px; height: 30px;
  background: var(--accent); color: #fff;
  display: flex; align-items: center; justify-content: center;
  font-size: 16px; border-radius: 2px;
}
.logo-text {
  font-weight: 600; font-size: 18px; color: var(--text);
  font-family: var(--font-serif); letter-spacing: 2px;
}
.header-center { flex: 1; max-width: 500px; margin: 0 auto; }
.search-box {
  display: flex; align-items: center; gap: 8px;
  background: var(--bg); border: 1px solid #e0dbd0;
  padding: 8px 16px; border-radius: 2px; cursor: text;
}
.search-placeholder { color: #c0b8a8; font-size: 13px; }
.header-right { display: flex; gap: 16px; font-size: 13px; color: var(--text-secondary); }
.nav-divider { color: var(--border); }
.nav-link { cursor: pointer; }
.nav-link:hover { color: var(--accent); }
</style>
