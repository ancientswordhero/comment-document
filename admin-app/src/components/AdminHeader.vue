<template>
  <header class="admin-header">
    <span class="admin-logo">云图书馆 · 管理后台</span>
    <div class="admin-links">
      <span>图书管理</span>
      <span class="divider">|</span>
      <span>管理员</span>
    </div>
  </header>
</template>

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
.admin-links { display: flex; gap: 16px; font-size: 13px; color: var(--text-secondary); }
.divider { color: var(--border); }
</style>
