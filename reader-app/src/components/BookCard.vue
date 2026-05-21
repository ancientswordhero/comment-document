<template>
  <div class="book-card" @click="$emit('select', book)">
    <div class="book-cover">
      <img v-if="book.coverUrl" :src="book.coverUrl" :alt="book.title" />
      <span v-else class="cover-placeholder">📖</span>
      <button
        v-if="isLoggedIn"
        class="shelf-toggle"
        :class="{ active: inShelf }"
        :title="inShelf ? '移出书架' : '加入书架'"
        @click.stop="toggleShelf"
      >{{ inShelf ? '📚' : '📖' }}</button>
    </div>
    <div class="book-title">{{ book.title }}</div>
    <div class="book-author">{{ book.author }}</div>
    <div v-if="book.categoryName" class="book-category">{{ book.categoryName }}</div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { addToBookshelf, removeFromBookshelf, checkBookshelf } from '../api/bookshelf'

const props = defineProps({ book: { type: Object, required: true } })
defineEmits(['select'])

const isLoggedIn = computed(() => !!localStorage.getItem('token'))
const inShelf = ref(false)

watch(() => props.book.id, async (id) => {
  if (isLoggedIn.value && id) {
    try {
      const data = await checkBookshelf(id)
      inShelf.value = data.inBookshelf
    } catch (e) { /* ignore */ }
  }
}, { immediate: true })

async function toggleShelf() {
  try {
    if (inShelf.value) {
      await removeFromBookshelf(props.book.id)
      inShelf.value = false
    } else {
      await addToBookshelf(props.book.id)
      inShelf.value = true
    }
  } catch (e) {
    console.error('书架操作失败:', e)
  }
}
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
.shelf-toggle {
  position: absolute;
  top: 6px;
  right: 6px;
  width: 28px;
  height: 28px;
  border: none;
  background: rgba(255,255,255,0.85);
  border-radius: 50%;
  font-size: 14px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s, background 0.2s;
}
.book-cover:hover .shelf-toggle { opacity: 1; }
.shelf-toggle.active { opacity: 1; background: var(--color-accent-light); }
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
