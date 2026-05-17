<template>
  <div class="home-layout">
    <div class="home-body">
      <CategoryNav
        :categories="categories"
        :selected-id="selectedCategoryId"
        :selected-child-id="selectedChildId"
        @select="onCategorySelect"
        @select-child="onChildSelect"
      />
      <div class="home-main">
        <BookCarousel />
        <SearchBar
          :categories="categories"
          @search="onSearch"
          @filter="onFilter"
        />
        <div v-if="loading" class="loading-text">加载中...</div>
        <div v-else class="book-grid">
          <BookCard v-for="book in books" :key="book.id" :book="book" />
        </div>
        <Pagination
          :page="page" :total="total" :size="20"
          @change="onPageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import CategoryNav from '../components/CategoryNav.vue'
import BookCarousel from '../components/BookCarousel.vue'
import SearchBar from '../components/SearchBar.vue'
import BookCard from '../components/BookCard.vue'
import Pagination from '../components/Pagination.vue'
import { getBooks, getCategories } from '../api/book'

const books = ref([])
const categories = ref([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)
const keyword = ref('')
const selectedCategoryId = ref(null)
const selectedChildId = ref(null)

onMounted(async () => {
  const catRes = await getCategories()
  categories.value = catRes.data
  fetchBooks()
})

async function fetchBooks() {
  loading.value = true
  try {
    const effectiveCategory = selectedChildId.value || selectedCategoryId.value
    const res = await getBooks({
      keyword: keyword.value || undefined,
      categoryId: effectiveCategory || undefined,
      page: page.value,
      size: 20
    })
    books.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function onSearch(kw) { keyword.value = kw; page.value = 1; fetchBooks() }
function onFilter(catId) {
  selectedCategoryId.value = catId
  selectedChildId.value = null
  page.value = 1; fetchBooks()
}
function onCategorySelect(catId) {
  selectedCategoryId.value = selectedCategoryId.value === catId ? null : catId
  selectedChildId.value = null
  page.value = 1; fetchBooks()
}
function onChildSelect(childId) {
  selectedChildId.value = childId
  page.value = 1; fetchBooks()
}
function onPageChange(p) { page.value = p; fetchBooks() }
</script>

<style scoped>
.home-layout { padding: 0 36px; }
.home-body { display: flex; gap: 24px; padding-top: 16px; }
.home-main { flex: 1; }
.book-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 14px; }
.loading-text { text-align: center; padding: 40px; color: var(--text-muted); }
</style>
