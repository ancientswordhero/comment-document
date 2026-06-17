<template>
  <div class="book-card" @click="$emit('select', book)">
    <!-- 封面区 -->
    <div class="book-cover">
      <img
        v-if="book.hasCover"
        :src="`/api/books/${book.id}/cover`"
        :alt="book.title"
      />
      <span v-else class="cover-placeholder">
        <span class="placeholder-icon">書</span>
      </span>
      <!-- 分类标签覆盖在封面底部 -->
      <span v-if="book.categoryName" class="cover-tag">{{ book.categoryName }}</span>
    </div>

    <!-- 信息区 -->
    <div class="book-body">
      <h3 class="book-title" :title="book.title">{{ book.title }}</h3>
      <p class="book-author">
        <span class="author-icon">✎</span>
        {{ book.author || '佚名' }}
      </p>
    </div>
  </div>
</template>

<script setup>
defineProps({ book: { type: Object, required: true } })
defineEmits(['select'])
</script>

<style scoped>
.book-card {
  background: var(--color-card-bg, #fff);
  border-radius: 6px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.25s, box-shadow 0.25s;
  box-shadow:
    0 1px 3px rgba(74,61,47,0.04),
    0 2px 8px rgba(74,61,47,0.06);
  display: flex;
  flex-direction: column;
  justify-self: center;
  width: 100%;
}
.book-card:hover {
  transform: translateY(-3px);
  box-shadow:
    0 4px 12px rgba(74,61,47,0.08),
    0 8px 24px rgba(74,61,47,0.10);
}

/* ---- 封面 ---- */
.book-cover {
  position: relative;
  aspect-ratio: 0.78;
  background: linear-gradient(160deg, #f7f3eb 0%, #ece4d5 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}
.book-cover img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  transition: transform 0.4s;
}
.book-card:hover .book-cover img {
  transform: scale(1.04);
}

/* 无封面占位 */
.cover-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
}
.placeholder-icon {
  font-size: 36px;
  color: #c8bb9e;
  font-family: var(--font-serif);
  opacity: 0.6;
}

/* 分类标签：覆盖封面底部 */
.cover-tag {
  position: absolute;
  bottom: 0; left: 0; right: 0;
  padding: 5px 8px;
  background: linear-gradient(transparent, rgba(0,0,0,0.55));
  color: #fff;
  font-size: 10px;
  font-family: var(--font-sans);
  letter-spacing: 1px;
  text-align: center;
  pointer-events: none;
}

/* ---- 信息区 ---- */
.book-body {
  padding: 8px 8px 10px;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.book-title {
  font-weight: 600;
  font-size: 13px;
  line-height: 1.5;
  color: var(--color-text, #4a3d2f);
  font-family: var(--font-serif);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin: 0;
}

.book-author {
  margin: 0;
  font-size: 11px;
  color: var(--color-text-muted, #a09880);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: flex;
  align-items: center;
  gap: 4px;
}
.author-icon {
  font-size: 10px;
  opacity: 0.5;
  flex-shrink: 0;
}
</style>
