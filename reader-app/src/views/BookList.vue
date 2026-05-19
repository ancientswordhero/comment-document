<template>
  <div class="home-layout">
    <BannerHeader />
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
          <BookCard v-for="book in books" :key="book.id" :book="book" />
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
import BannerHeader from '../components/BannerHeader.vue'
import CategoryNav from '../components/CategoryNav.vue'
import BookCard from '../components/BookCard.vue'
import Pagination from '../components/Pagination.vue'
import { getBooks, getCategories } from '../api/book'
import { useSearchState } from '../composables/useSearch'

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
</script>

<style scoped>
.home-layout { padding-top: 0; }
.home-body { display: flex; gap: 24px; padding: 20px var(--content-padding); }
.home-main { flex: 1; }
.book-grid {
  display: grid;
  grid-template-columns: repeat(var(--grid-cols), 1fr);
  gap: var(--grid-gap);
}
.loading-text, .empty-text { text-align: center; padding: 40px; color: var(--color-text-muted); }

@media (max-width: 768px) {
  .home-body { flex-direction: column; gap: 12px; padding: 12px var(--content-padding); }
  .book-grid { gap: 10px; }
}

@media (max-width: 480px) {
  .home-body { padding: 8px var(--content-padding); }
}
</style>
