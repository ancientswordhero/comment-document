<template>
  <div class="book-card" @click="$router.push(`/book/${book.id}`)">
    <div class="book-cover">
      <img v-if="book.coverUrl" :src="coverSrc" :alt="book.title" />
      <span v-else class="cover-placeholder">📖</span>
    </div>
    <div class="book-title">{{ book.title }}</div>
    <div class="book-author">{{ book.author }}</div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
const props = defineProps({ book: { type: Object, required: true } })
const coverSrc = computed(() =>
  props.book.coverUrl ? `http://localhost:8080${props.book.coverUrl}` : null
)
</script>

<style scoped>
.book-card {
  background: #fff; border: 1px solid var(--card-border);
  border-radius: 2px; padding: 12px; text-align: center;
  cursor: pointer; transition: box-shadow 0.2s, border-color 0.2s;
}
.book-card:hover {
  box-shadow: 0 3px 14px rgba(90,50,30,0.08);
  border-color: var(--accent);
}
.book-cover {
  background: #f8f5ee; height: 150px;
  display: flex; align-items: center; justify-content: center;
  margin-bottom: 10px; overflow: hidden;
}
.book-cover img { width: 100%; height: 100%; object-fit: cover; }
.cover-placeholder { font-size: 36px; color: #d0c8b4; }
.book-title { font-weight: 500; font-size: 13px; color: var(--text); }
.book-author { font-size: 11px; color: var(--text-muted); margin-top: 3px; }
</style>
