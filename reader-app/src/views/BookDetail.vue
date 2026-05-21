<template>
  <div v-if="errorMsg" class="error-page">
    <div class="error-card">
      <div class="error-icon">📕</div>
      <p class="error-text">{{ errorMsg }}</p>
      <button class="back-btn" @click="$router.push('/')">← 返回首页</button>
    </div>
  </div>

  <div class="detail-page" v-else-if="book">
    <div class="detail-card">
      <div class="detail-cover">
        <img v-if="book.coverUrl" :src="book.coverUrl" :alt="book.title" />
        <span v-else class="cover-placeholder">📖</span>
      </div>
      <div class="detail-info">
        <div class="detail-title-row">
          <h1 class="detail-title">{{ book.title }}</h1>
          <button
            v-if="isLoggedIn"
            class="shelf-btn"
            :class="{ active: inShelf }"
            @click="toggleShelf"
          >{{ inShelf ? '📚 移出书架' : '📖 加入书架' }}</button>
        </div>
        <div class="detail-meta">
          <div class="meta-item"><span class="meta-label">作者</span>{{ book.author }}</div>
          <div class="meta-item"><span class="meta-label">ISBN</span>{{ book.isbn }}</div>
          <div class="meta-item"><span class="meta-label">分类</span>{{ book.categoryName || '未分类' }}</div>
          <div class="meta-item"><span class="meta-label">上架时间</span>{{ formatDate(book.createdAt) }}</div>
        </div>
        <div class="detail-desc-title">图书简介</div>
        <div class="detail-desc" v-html="book.description || '暂无简介'"></div>
        <button class="back-btn" @click="$router.push('/')">← 返回首页</button>
      </div>
    </div>
  </div>

  <div v-else class="loading-text">加载中...</div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getBookById } from '../api/book'
import { addToBookshelf, removeFromBookshelf, checkBookshelf } from '../api/bookshelf'

const route = useRoute()
const book = ref(null)
const errorMsg = ref('')
const inShelf = ref(false)

const isLoggedIn = computed(() => !!localStorage.getItem('token'))

onMounted(async () => {
  try {
    book.value = await getBookById(route.params.id)
    if (isLoggedIn.value && book.value) {
      const data = await checkBookshelf(book.value.id)
      inShelf.value = data.inBookshelf
    }
  } catch (err) {
    const msg = err?.response?.data?.message || err?.message || '加载失败'
    errorMsg.value = msg
  }
})

async function toggleShelf() {
  if (!book.value) return
  try {
    if (inShelf.value) {
      await removeFromBookshelf(book.value.id)
      inShelf.value = false
    } else {
      await addToBookshelf(book.value.id)
      inShelf.value = true
    }
  } catch (e) {
    console.error('书架操作失败:', e)
  }
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleDateString('zh-CN')
}
</script>

<style scoped>
.detail-page { padding: 24px 32px; }
.detail-card {
  display: flex;
  gap: 28px;
  background: var(--color-card-bg);
  border: 1px solid var(--color-card-border);
  border-radius: var(--radius);
  padding: 24px;
}
.detail-cover {
  width: 260px;
  height: 360px;
  background: linear-gradient(135deg, #f5f1ea, #ede6d8);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  border-radius: var(--radius-sm);
  overflow: hidden;
}
.detail-cover img { width: 100%; height: 100%; object-fit: cover; }
.cover-placeholder { font-size: 64px; color: #d0c8b4; }
.detail-info { flex: 1; }
.detail-title-row {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}
.detail-title {
  font-size: 24px;
  font-family: var(--font-serif);
  color: var(--color-text);
  font-weight: 600;
  letter-spacing: 2px;
}
.shelf-btn {
  padding: 6px 16px;
  border: 1px solid var(--color-primary);
  background: transparent;
  color: var(--color-primary);
  border-radius: var(--radius);
  cursor: pointer;
  font-size: 13px;
  font-family: var(--font-serif);
  transition: all 0.2s;
  white-space: nowrap;
}
.shelf-btn:hover { background: var(--color-accent-light); }
.shelf-btn.active {
  background: var(--color-accent-light);
}
.detail-meta { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; margin-bottom: 24px; }
.meta-item { font-size: 13px; color: var(--color-text-secondary); }
.meta-label { color: var(--color-text-muted); margin-right: 8px; font-size: 12px; }
.detail-desc-title {
  font-size: 14px;
  font-family: var(--font-serif);
  color: var(--color-text);
  margin-bottom: 8px;
  font-weight: 600;
}
.detail-desc { font-size: 13px; color: var(--color-text-secondary); line-height: 1.8; }

/* 错误页面 */
.error-page { padding: 60px 32px; text-align: center; }
.error-card {
  display: inline-flex; flex-direction: column; align-items: center; gap: 16px;
  background: var(--color-card-bg); border: 1px solid var(--color-card-border);
  border-radius: var(--radius); padding: 40px 48px;
}
.error-icon { font-size: 48px; }
.error-text { font-size: 15px; color: var(--color-text-secondary); font-family: var(--font-serif); }

.back-btn {
  margin-top: 20px;
  padding: 8px 20px;
  background: var(--color-primary);
  color: #fff;
  border: none;
  border-radius: var(--radius);
  cursor: pointer;
  font-size: 13px;
  transition: background 0.2s;
}
.back-btn:hover { background: var(--color-primary-hover); }
.loading-text { text-align: center; padding: 60px; color: var(--color-text-muted); }

@media (max-width: 768px) {
  .detail-page { padding: 16px; }
  .detail-card { flex-direction: column; }
  .detail-cover { width: 100%; height: auto; aspect-ratio: 2/3; max-height: 400px; margin: 0 auto; }
}
</style>
