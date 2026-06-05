<template>
  <div class="bookshelf-page">
    <div class="bookshelf-header">
      <h1 class="bookshelf-title">我的书架</h1>
      <router-link to="/" class="back-link">← 返回首页</router-link>
    </div>

    <div v-if="loading" class="loading-text">加载中...</div>
    <div v-else-if="books.length === 0" class="empty-text">书架空空，去首页逛逛吧</div>
    <div v-else class="bookshelf-grid">
      <div v-for="book in books" :key="book.id" class="shelf-item">
        <div class="shelf-cover" @click="$router.push(`/book/${book.id}`)">
          <img v-if="book.hasCover" :src="`/api/books/${book.id}/cover`" :alt="book.title" />
          <span v-else class="cover-placeholder">📖</span>
        </div>
        <div class="shelf-info">
          <div class="shelf-book-title" @click="$router.push(`/book/${book.id}`)">{{ book.title }}</div>
          <div class="shelf-book-author">{{ book.author }}</div>
        </div>
        <button class="remove-btn" @click="removeBook(book)">移出书架</button>
      </div>
    </div>

    <Pagination
      v-if="total > 20"
      :page="page" :total="total" :size="20"
      @change="onPageChange"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getBookshelf, removeFromBookshelf } from '../api/bookshelf'
import Pagination from '../components/Pagination.vue'

const router = useRouter()
const books = ref([])
const loading = ref(true)
const page = ref(1)
const total = ref(0)

onMounted(() => fetchBookshelf())

async function fetchBookshelf() {
  loading.value = true
  try {
    const res = await getBookshelf(page.value, 20)
    books.value = res.records
    total.value = res.total
  } catch (e) {
    console.error('获取书架失败:', e)
  } finally {
    loading.value = false
  }
}

async function removeBook(book) {
  if (!confirm(`确定将《${book.title}》移出书架？`)) return
  try {
    await removeFromBookshelf(book.id)
    books.value = books.value.filter(b => b.id !== book.id)
    total.value--
  } catch (e) {
    console.error('移出书架失败:', e)
  }
}

function onPageChange(p) {
  page.value = p
  fetchBookshelf()
}
</script>

<style scoped>
.bookshelf-page {
  max-width: 960px;
  margin: 0 auto;
  padding: 24px 32px;
}

.bookshelf-header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 24px;
  padding-bottom: 12px;
  border-bottom: 2px solid var(--color-primary);
}

.bookshelf-title {
  font-size: 20px;
  font-family: var(--font-serif);
  color: var(--color-text);
  font-weight: 600;
  letter-spacing: 2px;
}

.back-link {
  font-size: 13px;
  color: var(--color-text-muted);
  text-decoration: none;
  transition: color 0.2s;
}
.back-link:hover { color: var(--color-primary); }

.loading-text, .empty-text {
  text-align: center;
  padding: 60px 0;
  color: var(--color-text-muted);
  font-family: var(--font-serif);
  font-size: 14px;
}

.bookshelf-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.shelf-item {
  display: flex;
  align-items: center;
  gap: 16px;
  background: var(--color-card-bg);
  border: 1px solid var(--color-card-border);
  border-radius: var(--radius);
  padding: 12px 16px;
  transition: box-shadow 0.2s;
}
.shelf-item:hover { box-shadow: var(--shadow-sm); }

.shelf-cover {
  width: 60px;
  height: 80px;
  background: linear-gradient(135deg, #f5f1ea, #ede6d8);
  border-radius: var(--radius-sm);
  overflow: hidden;
  cursor: pointer;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}
.shelf-cover img { width: 100%; height: 100%; object-fit: cover; }
.cover-placeholder { font-size: 20px; color: #d0c8b4; }

.shelf-info { flex: 1; min-width: 0; }
.shelf-book-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text);
  cursor: pointer;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.shelf-book-title:hover { color: var(--color-primary); }
.shelf-book-author {
  font-size: 12px;
  color: var(--color-text-muted);
  margin-top: 4px;
}

.remove-btn {
  padding: 5px 14px;
  border: 1px solid var(--color-border);
  background: var(--color-card-bg);
  color: var(--color-text-muted);
  border-radius: var(--radius);
  cursor: pointer;
  font-size: 12px;
  transition: all 0.2s;
  white-space: nowrap;
}
.remove-btn:hover {
  border-color: var(--color-danger);
  color: var(--color-danger);
  background: #fff5f5;
}

@media (max-width: 768px) {
  .bookshelf-page { padding: 16px; }
}
</style>
