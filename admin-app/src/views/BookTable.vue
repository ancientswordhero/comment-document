<template>
  <div class="table-page">
    <div class="toolbar">
      <input
        v-model="keyword" class="toolbar-search"
        placeholder="搜索书名、作者、ISBN..." @keyup.enter="search"
      />
      <select v-model="categoryId" class="toolbar-select" @change="search">
        <option :value="null">全部分类</option>
        <option v-for="cat in flatCategories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
      </select>
      <select v-model="statusFilter" class="toolbar-select" @change="search">
        <option :value="null">全部状态</option>
        <option :value="1">上架</option>
        <option :value="0">下架</option>
      </select>
      <button class="btn-add" @click="$router.push('/book/new')">+ 新增图书</button>
    </div>

    <table class="data-table">
      <thead>
        <tr><th>封面</th><th>书名</th><th>作者</th><th>ISBN</th><th>分类</th><th>状态</th><th>操作</th></tr>
      </thead>
      <tbody>
        <tr v-for="book in books" :key="book.id">
          <td>
            <div class="thumb">
              <img v-if="book.coverUrl" :src="'http://localhost:8080' + book.coverUrl" :alt="book.title" />
              <span v-else>封面</span>
            </div>
          </td>
          <td class="cell-title">{{ book.title }}</td>
          <td>{{ book.author }}</td>
          <td class="cell-isbn">{{ book.isbn }}</td>
          <td>{{ book.categoryName || '-' }}</td>
          <td>
            <span class="status-tag" :class="book.status === 1 ? 'up' : 'down'">
              {{ book.status === 1 ? '上架' : '下架' }}
            </span>
          </td>
          <td class="cell-actions">
            <a @click="$router.push(`/book/${book.id}/edit`)">编辑</a>
            <span class="sep">|</span>
            <a class="toggle" @click="onToggle(book)">{{ book.status === 1 ? '下架' : '上架' }}</a>
            <span class="sep">|</span>
            <a class="del" @click="onDelete(book)">删除</a>
          </td>
        </tr>
      </tbody>
    </table>

    <div class="table-pagination" v-if="totalPages > 1">
      <button :disabled="page <= 1" @click="goPage(page - 1)">← 上一页</button>
      <span class="page-info">{{ page }} / {{ totalPages }}</span>
      <button :disabled="page >= totalPages" @click="goPage(page + 1)">下一页 →</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getBooks, deleteBook, toggleStatus, getCategories } from '../api/book'

const books = ref([])
const flatCategories = ref([])
const keyword = ref('')
const categoryId = ref(null)
const statusFilter = ref(null)
const page = ref(1)
const total = ref(0)
const size = 20
const totalPages = computed(() => Math.ceil(total.value / size))

function flattenCategories(cats, prefix = '') {
  let result = []
  for (const cat of cats) {
    result.push({ id: cat.id, name: prefix + cat.name })
    if (cat.children) result.push(...flattenCategories(cat.children, '  ' + prefix))
  }
  return result
}

onMounted(async () => {
  const res = await getCategories()
  flatCategories.value = flattenCategories(res.data)
  fetchBooks()
})

async function fetchBooks() {
  const res = await getBooks({
    keyword: keyword.value || undefined,
    categoryId: categoryId.value || undefined,
    status: statusFilter.value,
    page: page.value, size
  })
  books.value = res.data.records
  total.value = res.data.total
}

function search() { page.value = 1; fetchBooks() }
function goPage(p) { page.value = p; fetchBooks() }

async function onToggle(book) {
  await toggleStatus(book.id); fetchBooks()
}
async function onDelete(book) {
  if (confirm(`确定删除「${book.title}」吗？`)) {
    await deleteBook(book.id); fetchBooks()
  }
}
</script>

<style scoped>
.table-page { padding: 16px 28px; }
.toolbar { display: flex; gap: 8px; margin-bottom: 14px; align-items: center; }
.toolbar-search {
  flex: 1; max-width: 280px; padding: 7px 12px;
  border: 1px solid #e0dbd0; border-radius: 2px; font-size: 12px; outline: none;
}
.toolbar-select {
  padding: 7px 12px; border: 1px solid #e0dbd0; border-radius: 2px;
  font-size: 12px; color: var(--text-secondary); outline: none;
}
.btn-add {
  margin-left: auto; padding: 8px 20px;
  background: var(--accent); color: #fff; border: none;
  border-radius: 2px; font-size: 12px; cursor: pointer;
}
.data-table { width: 100%; border-collapse: collapse; font-size: 12px; }
.data-table th {
  text-align: left; padding: 10px 12px; color: var(--text-secondary);
  font-weight: 500; border-bottom: 2px solid var(--border);
}
.data-table td { padding: 10px 12px; border-bottom: 1px solid var(--accent-light); }
.thumb {
  width: 36px; height: 48px; background: #f5f1ea;
  display: flex; align-items: center; justify-content: center;
  font-size: 10px; color: var(--text-muted); overflow: hidden;
}
.thumb img { width: 100%; height: 100%; object-fit: cover; }
.cell-title { color: var(--text); font-weight: 500; }
.cell-isbn { color: var(--text-muted); font-size: 11px; }
.status-tag { padding: 2px 8px; border-radius: 2px; font-size: 11px; }
.status-tag.up { background: #e8f5e9; color: #5b8c5a; }
.status-tag.down { background: #fff3e0; color: #c08840; }
.cell-actions a { cursor: pointer; color: var(--text-secondary); }
.cell-actions a:hover { color: var(--accent); }
.cell-actions a.del:hover { color: #c04040; }
.cell-actions .sep { color: var(--border); margin: 0 6px; }
.table-pagination {
  text-align: center; margin-top: 16px;
  display: flex; align-items: center; justify-content: center; gap: 12px; font-size: 12px;
}
.table-pagination button {
  padding: 3px 12px; border: 1px solid #e0dbd0;
  background: #fff; border-radius: 2px; color: var(--text-secondary); cursor: pointer;
}
.page-info { color: var(--text-muted); }
</style>
