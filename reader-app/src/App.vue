<template>
  <div class="app">
    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<style scoped>
.app { min-height: 100vh; display: flex; flex-direction: column; }
.main-content { flex: 1; }
</style>
