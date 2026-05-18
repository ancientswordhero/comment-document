<template>
  <header class="admin-header">
    <span class="admin-logo">云图书馆 · 管理后台</span>
    <div class="admin-links">
      <router-link to="/">图书管理</router-link>
      <span class="divider">|</span>
      <router-link to="/admins">新增管理员</router-link>
      <span class="divider">|</span>
      <a @click="logout">退出</a>
    </div>
  </header>
</template>

<script setup>
function logout() {
  localStorage.removeItem('token')
  window.location.href = 'http://localhost:5175'
}
</script>

<style scoped>
.admin-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 12px 28px; background: var(--header-bg);
  border-bottom: 1px solid var(--border);
}
.admin-logo {
  font-weight: 600; font-size: 16px; color: var(--text);
  font-family: var(--font-serif); letter-spacing: 2px;
}
.admin-links { display: flex; gap: 16px; font-size: 13px; color: var(--text-secondary); align-items: center; }
.admin-links a { cursor: pointer; }
.admin-links a:hover { color: var(--accent); }
.divider { color: var(--border); }
</style>
