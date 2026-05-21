<template>
  <div class="home-layout">
    <BannerHeader />
    <div class="header-divider" />
    <div class="home-body">
      <CategoryNav
        :categories="categories"
        :selected-id="selectedCategoryId"
        :selected-child-id="selectedChildId"
        @select="onCategorySelect"
        @select-child="onChildSelect"
      />
      <div class="home-main">
        <div v-if="loading" class="loading-text">加载中...</div>
        <div v-else-if="books.length === 0" class="empty-text">暂无图书</div>
        <div v-else class="book-grid">
          <BookCard v-for="book in books" :key="book.id" :book="book" @select="onBookSelect" />
        </div>
        <Pagination
          v-if="total > 0"
          :page="page" :total="total" :size="20"
          @change="onPageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import BannerHeader from '../components/BannerHeader.vue'
import CategoryNav from '../components/CategoryNav.vue'
import BookCard from '../components/BookCard.vue'
import Pagination from '../components/Pagination.vue'
import { getBooks, getCategories } from '../api/book'
import { useSearchState } from '../composables/useSearch'

const router = useRouter()

const books = ref([])
const categories = ref([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)
const selectedCategoryId = ref(null)
const selectedChildId = ref(null)

const { searchKeyword } = useSearchState()

onMounted(async () => {
  await fetchCategories()
  fetchBooks()
})

async function fetchCategories() {
  try {
    const catRes = await getCategories()
    categories.value = catRes
  } catch (err) {
    console.error('获取分类失败:', err)
  }
}

async function fetchBooks() {
  loading.value = true
  try {
    const effectiveCategory = selectedChildId.value || selectedCategoryId.value
    const res = await getBooks({
      keyword: searchKeyword.value || undefined,
      categoryId: effectiveCategory || undefined,
      page: page.value,
      size: 20
    })
    books.value = res.records
    total.value = res.total
  } catch (err) {
    console.error('获取图书失败:', err)
  } finally {
    loading.value = false
  }
}

watch(searchKeyword, () => {
  page.value = 1
  fetchBooks()
})

function onCategorySelect(catId) {
  selectedCategoryId.value = selectedCategoryId.value === catId ? null : catId
  selectedChildId.value = null
  page.value = 1
  fetchBooks()
}

function onChildSelect(childId) {
  selectedChildId.value = childId
  page.value = 1
  fetchBooks()
}

function onPageChange(p) {
  page.value = p
  fetchBooks()
}

function onBookSelect(book) {
  if (!localStorage.getItem('token')) {
    alert('请登录后查看完整图书信息')
    if (confirm('是否前往登录？')) {
      window.location.href = `http://localhost:5176?redirect=${encodeURIComponent(`/book/${book.id}`)}`
    }
    return
  }
  router.push(`/book/${book.id}`)
}
</script>

<style scoped>
.home-layout { padding-top: 0; }

.header-divider {
  position: relative;
  z-index: 5;
  height: 6px;
  margin-top: -2px;
  background: linear-gradient(
    180deg,
    var(--color-border) 0%,
    var(--color-accent-light) 40%,
    var(--color-bg) 100%
  );
}

.home-body {
  position: relative;
  z-index: 4;
  display: flex;
  gap: 24px;
  padding: 0 var(--content-padding) 32px;
  min-height: calc(100vh - 300px);
}
.home-main { 
  flex: 1; 
  margin-top: 0;
}
.book-grid {
  display: grid;
  grid-template-columns: repeat(var(--grid-cols), 1fr);
  gap: var(--grid-gap);
  margin-top: 24px;
  align-items: start;
}
.loading-text, .empty-text { text-align: center; padding: 40px; color: var(--color-text-muted); }

@media (max-width: 768px) {
  .home-body { flex-direction: column; gap: 20px; padding: 0 var(--content-padding) 20px; }
  .home-main { margin-top: 0; }
  .book-grid { gap: 12px; margin-top: 16px; }
}

@media (max-width: 480px) {
  .home-body { padding: 0 var(--content-padding) 16px; gap: 16px; }
  .home-main { margin-top: 0; }
  .book-grid { margin-top: 12px; }
}
</style>
