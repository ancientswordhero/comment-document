<template>
  <div class="search-bar">
    <input
      v-model="keywords"
      class="search-input"
      placeholder="搜索书名 · 作者 · ISBN"
      @keyup.enter="$emit('search', keywords)"
    />
    <select v-model="categoryId" class="search-select" @change="$emit('filter', categoryId)">
      <option :value="null">全部分类</option>
      <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
    </select>
    <button class="search-btn" @click="$emit('search', keywords)">搜索</button>
  </div>
</template>

<script setup>
import { ref } from 'vue'
defineProps({ categories: { type: Array, default: () => [] } })
defineEmits(['search', 'filter'])
const keywords = ref('')
const categoryId = ref(null)
</script>

<style scoped>
.search-bar { display: flex; gap: 8px; margin-bottom: 14px; }
.search-input {
  flex: 1; max-width: 320px; padding: 7px 12px;
  background: #fff; border: 1px solid #e0dbd0; border-radius: 2px;
  font-size: 12px; color: var(--text); outline: none;
}
.search-input:focus { border-color: var(--accent); }
.search-input::placeholder { color: #c0b8a8; }
.search-select {
  padding: 7px 12px; background: #fff;
  border: 1px solid #e0dbd0; border-radius: 2px;
  font-size: 12px; color: var(--text-secondary); outline: none;
}
.search-btn {
  padding: 7px 16px; background: var(--accent); color: #fff;
  border: none; border-radius: 2px; font-size: 12px; cursor: pointer;
}
.search-btn:hover { opacity: 0.9; }
</style>
