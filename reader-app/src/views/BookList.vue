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
        <!-- 搜索栏：融入主内容区 -->
        <div class="main-search-row">
          <div class="main-search">
            <input
              v-model="searchKeywords"
              class="main-search-input"
              placeholder="搜索书名 · 作者 · ISBN"
              @keyup.enter="onSearch"
            />
            <button type="button" class="main-search-btn" @click="onSearch">搜索</button>
          </div>
          <span class="main-tagline">{{ currentTagline }}</span>
        </div>

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
import { ref, watch, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'

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
const searchKeywords = ref('')

// 轮换 Tagline
const taglines = [
  '万卷古今消永日 · 一窗昏晓送流年',
  '书卷多情似故人 · 晨昏忧乐每相亲',
  '书山有路勤为径 · 学海无涯苦作舟',
  '腹有诗书气自华 · 最是书香能致远',
  '枕上诗书闲处好 · 门前风景雨来佳',
  '蹉跎莫遣韶光老 · 人生唯有读书好',
]
const taglineIndex = ref(0)
const currentTagline = ref(taglines[0])
let taglineTimer = null

function cycleTagline() {
  const el = document.querySelector('.main-tagline')
  if (el) el.style.opacity = '0'
  setTimeout(() => {
    taglineIndex.value = (taglineIndex.value + 1) % taglines.length
    currentTagline.value = taglines[taglineIndex.value]
    requestAnimationFrame(() => {
      if (el) el.style.opacity = '1'
    })
  }, 360)
}

const { searchKeyword } = useSearchState()

onMounted(async () => {
  await fetchCategories()
  fetchBooks()
  taglineTimer = setInterval(cycleTagline, 6000)
})

onBeforeUnmount(() => {
  if (taglineTimer) clearInterval(taglineTimer)
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

function onSearch() {
  const keyword = searchKeywords.value.trim()
  searchKeyword.value = keyword
}

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

function goDiscover() {
  window.location.href = '/scene/index.html'
}
</script>

<style scoped>
.home-layout { padding-top: 0; }

/* ---- 主体布局 ---- */
.home-body {
  display: flex;
  gap: 24px;
  padding: 0 var(--content-padding) 32px;
  padding-top: 26px;
}

.home-main {
  flex: 1;
  min-width: 0;
}

/* ---- 搜索栏（融入 home-main 顶部） ---- */
.main-search-row {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding-bottom: 20px;
  margin-bottom: 8px;
  border-bottom: 1px solid var(--color-border, #e8e4dc);
}

.main-search {
  display: flex;
  align-items: center;
  width: 100%;
  max-width: 520px;
  background: #fff;
  border-radius: 20px;
  border: 1px solid var(--color-border, #e8e4dc);
  overflow: hidden;
  transition: border-color 0.2s, box-shadow 0.2s;
}
.main-search:focus-within {
  border-color: var(--color-primary, #c9a96e);
  box-shadow: 0 0 0 3px rgba(201,169,110,0.12);
}
.main-search-input {
  flex: 1;
  border: none;
  padding: 8px 16px;
  font-size: 13px;
  line-height: 1.5;
  color: var(--color-text, #4a3d2f);
  outline: none;
  background: transparent;
  font-family: var(--font-sans);
  min-width: 0;
}
.main-search-input::placeholder { color: var(--color-text-muted, #a09880); }
.main-search-btn {
  padding: 8px 20px;
  background: var(--color-primary, #c9a96e);
  color: #fff;
  border: none;
  font-size: 13px;
  line-height: 1.5;
  cursor: pointer;
  font-family: var(--font-serif);
  letter-spacing: 1px;
  transition: background 0.2s;
  white-space: nowrap;
  align-self: stretch;
}
.main-search-btn:hover { background: var(--color-primary-hover, #b8944d); }

.main-tagline {
  font-size: 11px;
  color: var(--color-text-muted, #a09880);
  font-family: var(--font-serif);
  letter-spacing: 2px;
  transition: opacity 0.6s;
}

/* ---- 图书网格 ---- */
.book-grid {
  display: grid;
  grid-template-columns: repeat(var(--grid-cols), 1fr);
  gap: var(--grid-gap);
  margin-top: 8px;
  align-items: start;
}
.loading-text, .empty-text { text-align: center; padding: 40px; color: var(--color-text-muted); }

/* ---- 响应式 ---- */
@media (max-width: 1024px) {
  .main-tagline { display: none; }
}
@media (max-width: 768px) {
  .home-body { flex-direction: column; gap: 16px; padding: 12px var(--content-padding) 20px; }
  .main-search-row { padding-bottom: 12px; }
  .main-search { max-width: none; }
  .book-grid { gap: 12px; margin-top: 4px; }
}
@media (max-width: 480px) {
  .home-body { padding: 10px var(--content-padding) 16px; }
  .main-search-row { gap: 8px; padding-bottom: 10px; }
  .main-search-input { padding: 7px 10px; font-size: 12px; }
  .main-search-btn { padding: 7px 14px; font-size: 11px; }
}
</style>
