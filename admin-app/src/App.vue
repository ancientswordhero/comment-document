<template>
  <div class="admin-app">
    <AdminHeader />
    <router-view />
  </div>
</template>

<script setup>
import AdminHeader from './components/AdminHeader.vue'
</script>

<style scoped>
.admin-app { min-height: 100vh; background: var(--bg); }
</style>
