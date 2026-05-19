<template>
  <div class="book-card" @click="$router.push(`/book/${book.id}`)">
    <div class="book-cover">
      <img v-if="book.coverUrl" :src="coverSrc" :alt="book.title" />
      <span v-else class="cover-placeholder">📖</span>
    </div>
    <div class="book-title">{{ book.title }}</div>
    <div class="book-author">{{ book.author }}</div>
    <div v-if="book.categoryName" class="book-category">{{ book.categoryName }}</div>
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
  background: var(--color-card-bg);
  border: 1px solid var(--color-card-border);
  border-radius: var(--radius);
  padding: 0 0 var(--card-padding) 0;
  text-align: center;
  cursor: pointer;
  transition: box-shadow 0.2s, border-color 0.2s, transform 0.2s;
  box-shadow: var(--shadow-sm);
  overflow: hidden;
}
.book-card:hover {
  box-shadow: var(--shadow-md);
  border-color: var(--color-primary);
  transform: translateY(-2px);
}
.book-cover {
  background: linear-gradient(135deg, #f5f1ea, #ede6d8);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  aspect-ratio: 2 / 3;
}
.book-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.cover-placeholder {
  font-size: 36px;
  color: #d0c8b4;
}
.book-title {
  font-weight: 500;
  font-size: 13px;
  color: var(--color-text);
  margin-top: 10px;
  padding: 0 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.book-author {
  font-size: 11px;
  color: var(--color-text-muted);
  margin-top: 3px;
  padding: 0 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.book-category {
  margin-top: 6px;
  padding: 2px 8px;
  background: var(--color-accent-light);
  color: var(--color-primary);
  border-radius: 10px;
  font-size: 10px;
  display: inline-block;
}
</style>
