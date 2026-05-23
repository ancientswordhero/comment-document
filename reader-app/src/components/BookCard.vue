<template>
  <div class="book-card" @click="$emit('select', book)">
    <div class="book-cover">
      <img v-if="book.coverUrl" :src="book.coverUrl" :alt="book.title" />
      <span v-else class="cover-placeholder">📖</span>
    </div>
    <div class="book-title">{{ book.title }}</div>
    <div class="book-author">{{ book.author }}</div>
    <div v-if="book.categoryName" class="book-category">{{ book.categoryName }}</div>
  </div>
</template>

<script setup>
defineProps({ book: { type: Object, required: true } })
defineEmits(['select'])
</script>

<style scoped>
.book-card {
  width: 220px;
  background: var(--color-card-bg);
  border: 1px solid var(--color-card-border);
  border-radius: var(--radius);
  padding: 0 0 calc(var(--card-padding) * 0.8) 0;
  text-align: center;
  cursor: pointer;
  transition: box-shadow 0.2s, border-color 0.2s, transform 0.2s;
  box-shadow: var(--shadow-sm);
  overflow: hidden;
  justify-self: center;
}
.book-card:hover {
  box-shadow: var(--shadow-md);
  border-color: var(--color-primary);
  transform: translateY(-2px);
}
.book-cover {
  position: relative;
  background: linear-gradient(135deg, #f5f1ea, #ede6d8);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  aspect-ratio: 2 / 3;
  max-height: 220px;
}
.book-cover img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}
.cover-placeholder {
  font-size: 28px;
  color: #d0c8b4;
}
.book-title {
  font-weight: 500;
  font-size: 12px;
  color: var(--color-text);
  margin-top: 8px;
  padding: 0 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.book-author {
  font-size: 10px;
  color: var(--color-text-muted);
  margin-top: 2px;
  padding: 0 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.book-category {
  margin-top: 4px;
  padding: 2px 6px;
  background: var(--color-accent-light);
  color: var(--color-primary);
  border-radius: 8px;
  font-size: 9px;
  display: inline-block;
}
</style>
