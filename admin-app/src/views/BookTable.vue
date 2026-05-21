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

    <div class="table-wrapper">
    <table class="data-table">
      <thead>
        <tr><th>封面</th><th>书名</th><th>作者</th><th>ISBN</th><th>分类</th><th>状态</th><th>操作</th></tr>
      </thead>
      <tbody>
        <tr v-for="book in books" :key="book.id">
          <td>
            <div class="thumb">
              <img v-if="book.coverUrl" :src="book.coverUrl" :alt="book.title" />
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
    </div>

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
  try {
    const catRes = await getCategories()
    flatCategories.value = flattenCategories(catRes)
  } catch (err) {
    console.error('获取分类失败:', err)
  }
  fetchBooks()
})

async function fetchBooks() {
  try {
    const res = await getBooks({
      keyword: keyword.value || undefined,
      categoryId: categoryId.value || undefined,
      status: statusFilter.value,
      page: page.value, size
    })
    books.value = res.records
    total.value = res.total
  } catch (err) {
    console.error('获取图书列表失败:', err)
  }
}

function search() { page.value = 1; fetchBooks() }
function goPage(p) { page.value = p; fetchBooks() }

async function onToggle(book) {
  try {
    await toggleStatus(book.id)
    fetchBooks()
  } catch (err) {
    console.error('切换状态失败:', err)
  }
}

async function onDelete(book) {
  if (confirm(`确定删除「${book.title}」吗？`)) {
    try {
      await deleteBook(book.id)
      fetchBooks()
    } catch (err) {
      console.error('删除图书失败:', err)
    }
  }
}
</script>

<style scoped>
.table-page { padding: var(--content-padding); }
.toolbar { display: flex; gap: 8px; margin-bottom: 14px; align-items: center; flex-wrap: wrap; }
.toolbar-search {
  flex: 1; min-width: 160px; max-width: 280px; padding: 8px 12px;
  border: 1px solid var(--color-border); border-radius: var(--radius);
  font-size: 12px; outline: none; color: var(--color-text);
}
.toolbar-search::placeholder { color: var(--color-text-muted); }
.toolbar-select {
  padding: 8px 12px; border: 1px solid var(--color-border); border-radius: var(--radius);
  font-size: 12px; color: var(--color-text-secondary); outline: none; background: var(--color-card-bg);
}
.btn-add {
  margin-left: auto; padding: 8px 20px;
  background: var(--color-primary); color: #fff; border: none;
  border-radius: var(--radius); font-size: 12px; cursor: pointer;
  transition: background 0.2s;
}
.btn-add:hover { background: var(--color-primary-hover); }
.table-wrapper { overflow-x: auto; }
.data-table {
  width: 100%; border-collapse: collapse; font-size: 12px; min-width: 700px;
}
.data-table th {
  text-align: left; padding: 10px 12px; color: var(--color-text-secondary);
  font-weight: 500; border-bottom: 2px solid var(--color-border);
  background: var(--color-bg);
}
.data-table td { padding: 10px 12px; border-bottom: 1px solid var(--color-accent-light); }
.thumb {
  width: 36px; height: 48px; background: var(--color-accent-light);
  display: flex; align-items: center; justify-content: center;
  font-size: 10px; color: var(--color-text-muted); overflow: hidden;
  border-radius: var(--radius-sm);
}
.thumb img { width: 100%; height: 100%; object-fit: cover; object-position: center; }
.cell-title { color: var(--color-text); font-weight: 500; }
.cell-isbn { color: var(--color-text-muted); font-size: 11px; }
.status-tag {
  padding: 2px 10px; border-radius: 12px; font-size: 11px; font-weight: 500;
}
.status-tag.up { background: #e8f5e9; color: var(--color-success); }
.status-tag.down { background: #fff3e0; color: var(--color-warning); }
.cell-actions a { cursor: pointer; color: var(--color-text-secondary); }
.cell-actions a:hover { color: var(--color-primary); }
.cell-actions a.del:hover { color: var(--color-danger); }
.cell-actions .sep { color: var(--color-border); margin: 0 4px; }
.table-pagination {
  text-align: center; margin-top: 16px;
  display: flex; align-items: center; justify-content: center; gap: 12px; font-size: 12px;
}
.table-pagination button {
  padding: 5px 14px; border: 1px solid var(--color-border);
  background: var(--color-card-bg); border-radius: var(--radius);
  color: var(--color-text-secondary); cursor: pointer;
  transition: all 0.2s;
}
.table-pagination button:hover:not(:disabled) {
  border-color: var(--color-primary); color: var(--color-primary);
}
.table-pagination button:disabled { color: #d0c8b4; cursor: not-allowed; }
.page-info { color: var(--color-text-muted); }

@media (max-width: 768px) {
  .toolbar { gap: 6px; }
  .toolbar-search { min-width: 120px; }
  .btn-add { padding: 6px 14px; font-size: 11px; }
}

@media (max-width: 480px) {
  .toolbar { flex-direction: column; align-items: stretch; }
  .toolbar-search { max-width: 100%; }
  .btn-add { margin-left: 0; text-align: center; }
  .table-pagination { flex-wrap: wrap; gap: 8px; }
}
</style>