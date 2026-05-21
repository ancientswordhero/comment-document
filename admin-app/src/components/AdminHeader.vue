<template>
  <header class="app-header">
    <div class="header-left">
      <span class="logo-icon">書</span>
      <span class="logo-text">云图书馆 · 管理后台</span>
    </div>
    <div class="header-right">
      <router-link class="nav-link desktop-only" to="/">图书管理</router-link>
      <span class="nav-divider desktop-only">|</span>
      <router-link class="nav-link desktop-only" to="/reports">举报管理</router-link>
      <span class="nav-divider desktop-only">|</span>
      <router-link class="nav-link desktop-only" to="/admins">新增管理员</router-link>
      <span class="nav-divider desktop-only">|</span>
      <span class="nav-user desktop-only">{{ username }}</span>
      <span class="nav-divider desktop-only">|</span>
      <span class="nav-link" @click="logout">退出</span>
      <button class="mobile-menu-btn mobile-only" @click="toggleMenu">☰</button>
    </div>
    <div class="mobile-menu" v-if="menuOpen">
      <router-link class="mobile-menu-item" to="/" @click="menuOpen = false">图书管理</router-link>
      <router-link class="mobile-menu-item" to="/reports" @click="menuOpen = false">举报管理</router-link>
      <router-link class="mobile-menu-item" to="/admins" @click="menuOpen = false">新增管理员</router-link>
      <span class="mobile-menu-item" @click="logout">退出</span>
    </div>
  </header>
</template>

<script setup>
import { ref, computed } from 'vue'

const menuOpen = ref(false)
const username = computed(() => localStorage.getItem('username') || '管理员')

function logout() {
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  window.location.href = 'http://localhost:5174'
}

function toggleMenu() {
  menuOpen.value = !menuOpen.value
}
</script>

<style scoped>
.app-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: var(--header-padding); background: var(--color-header-bg);
  border-bottom: 1px solid var(--color-border);
  box-shadow: var(--shadow-sm);
  position: sticky; top: 0; z-index: 100;
}
.header-left { display: flex; align-items: center; gap: 8px; }
.logo-icon {
  width: 30px; height: 30px;
  background: var(--color-primary); color: #fff;
  display: flex; align-items: center; justify-content: center;
  font-size: 16px; border-radius: var(--radius);
}
.logo-text {
  font-weight: 600; font-size: 15px; color: var(--color-text);
  font-family: var(--font-serif); letter-spacing: 2px;
}
.header-right { display: flex; align-items: center; gap: 4px; font-size: 12px; color: var(--color-text-secondary); }
.nav-divider { color: var(--color-border); }
.nav-user {
  font-size: 12px; color: var(--color-text-secondary);
  font-family: var(--font-serif);
}
.nav-link {
  cursor: pointer;
  padding: 5px 10px;
  border-radius: var(--radius);
  transition: all 0.2s;
}
.nav-link:hover { background: var(--color-accent-light); color: var(--color-primary); }
.nav-link:last-of-type:hover { background: #fff3f3; color: var(--color-danger); }
.mobile-menu-btn { display: none; background: none; border: none; font-size: 20px; cursor: pointer; color: var(--color-text); }
.mobile-menu {
  display: none;
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: var(--color-header-bg);
  border-bottom: 1px solid var(--color-border);
  padding: 12px;
  box-shadow: var(--shadow-md);
  flex-direction: column;
  gap: 8px;
}
.mobile-menu-item {
  display: block;
  padding: 10px 16px;
  background: var(--color-accent-light);
  color: var(--color-primary);
  border-radius: var(--radius);
  text-align: center;
  cursor: pointer;
}
.mobile-menu-item:last-child { background: #fff3f3; color: var(--color-danger); }

@media (max-width: 768px) {
  .desktop-only { display: none; }
  .mobile-only { display: block; }
  .mobile-menu { display: flex; }
  .logo-text { font-size: 13px; }
}

@media (max-width: 480px) {
  .logo-text { display: none; }
}
</style>
