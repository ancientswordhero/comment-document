<template>
  <div class="app">
    <AppHeader />
    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import AppHeader from './components/AppHeader.vue'
</script>

<style scoped>
.app { min-height: 100vh; display: flex; flex-direction: column; }
.main-content { flex: 1; }
</style>
