<template>
  <div class="admin-app">
    <AdminHeader />
    <div class="header-divider" />
    <div class="body-wrapper">
      <router-view />
    </div>
    <footer class="app-footer">
      <span class="footer-line" />
      <span class="footer-text">書 · 云图书馆管理后台</span>
      <span class="footer-line" />
    </footer>
  </div>
</template>

<script setup>
import AdminHeader from './components/AdminHeader.vue'
</script>

<style scoped>
.admin-app {
  min-height: 100vh;
  background: var(--color-bg);
  display: flex;
  flex-direction: column;
}

.header-divider {
  height: 8px;
  background: linear-gradient(
    180deg,
    var(--color-header-bg) 0%,
    var(--color-accent-light) 40%,
    var(--color-bg) 100%
  );
  position: sticky;
  top: 48px;
  z-index: 99;
}

.body-wrapper {
  flex: 1;
  padding: var(--content-padding);
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
}

.footer-line {
  width: 60px;
  height: 1px;
  background: var(--color-border);
}
</style>
