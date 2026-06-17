<template>
  <div class="app">
    <BannerHeader />
    <main class="main-content">
      <router-view />
    </main>
    <footer class="app-footer">
      <span class="footer-line" />
      <span class="footer-text">書 · 云图书馆</span>
      <span class="footer-line" />
    </footer>
  </div>
</template>

<script setup>
import BannerHeader from './components/BannerHeader.vue'
</script>

<style scoped>
.app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
}

.app-footer {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 24px;
  margin-top: auto;
  color: var(--color-text-muted);
  font-size: 13px;
  font-family: var(--font-serif);
  letter-spacing: 2px;
  background: var(--color-bg);
}

.footer-line {
  width: 60px;
  height: 1px;
  background: var(--color-border);
}
</style>
