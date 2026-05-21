<template>
  <div class="report-page">
    <h2 class="page-title">举报管理</h2>
    <div class="filter-bar">
      <select v-model="statusFilter" class="filter-select" @change="search">
        <option value="">全部</option>
        <option value="pending">待处理</option>
        <option value="deleted">已删除</option>
        <option value="dismissed">已驳回</option>
      </select>
    </div>
    <table class="data-table" v-if="reports.length > 0">
      <thead>
        <tr><th>书评内容</th><th>所属图书</th><th>举报人</th><th>被举报人</th><th>理由</th><th>状态</th><th>时间</th><th>操作</th></tr>
      </thead>
      <tbody>
        <tr v-for="r in reports" :key="r.id">
          <td class="cell-content">{{ r.reviewContent || '(已删除)' }}</td>
          <td>{{ r.bookTitle }}</td>
          <td>{{ r.reporterName }}</td>
          <td>{{ r.reviewAuthorName }}</td>
          <td>{{ reasonLabel(r.reason) }}<span v-if="r.detail">: {{ r.detail }}</span></td>
          <td><span class="status-tag" :class="r.status">{{ statusLabel(r.status) }}</span></td>
          <td>{{ formatDate(r.createdAt) }}</td>
          <td class="cell-actions" v-if="r.status === 'pending'">
            <a @click="handleResolve(r, 'delete')">删除评论</a>
            <span class="sep">|</span>
            <a @click="handleResolve(r, 'dismiss')">驳回举报</a>
          </td>
          <td v-else class="cell-muted">已处理</td>
        </tr>
      </tbody>
    </table>
    <div v-else-if="!loading" class="empty-text">暂无举报</div>
    <div class="pagination" v-if="totalPages > 1">
      <button :disabled="page <= 1" @click="goPage(page - 1)">← 上一页</button>
      <span class="page-info">{{ page }} / {{ totalPages }}</span>
      <button :disabled="page >= totalPages" @click="goPage(page + 1)">下一页 →</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getReports, resolveReport } from '../api/report'

const reports = ref([]); const loading = ref(false)
const page = ref(1); const totalCount = ref(0)
const statusFilter = ref(''); const pageSize = 10
const totalPages = computed(() => Math.ceil(totalCount.value / pageSize) || 0)

onMounted(() => fetchReports())

async function fetchReports() {
  loading.value = true
  try {
    const data = await getReports({ status: statusFilter.value || undefined, page: page.value, size: pageSize })
    reports.value = data.records; totalCount.value = data.total
  } finally { loading.value = false }
}

async function handleResolve(report, action) {
  const label = action === 'delete' ? '删除评论' : '驳回举报'
  if (!confirm(`确定${label}吗？`)) return
  await resolveReport(report.id, { action }); fetchReports()
}

function search() { page.value = 1; fetchReports() }
function goPage(p) { page.value = p; fetchReports() }

function reasonLabel(r) {
  const map = { spam: '垃圾广告', abuse: '人身攻击', fake: '虚假信息', violation: '违规内容', other: '其他' }
  return map[r] || r
}
function statusLabel(s) {
  const map = { pending: '待处理', deleted: '已删除', dismissed: '已驳回' }
  return map[s] || s
}
function formatDate(d) { return d ? new Date(d).toLocaleDateString('zh-CN') : '' }
</script>

<style scoped>
.report-page { padding: 20px 28px; }
.page-title { font-size: 20px; font-family: var(--font-serif); color: var(--color-text); letter-spacing: 2px; margin-bottom: 16px; }
.filter-bar { margin-bottom: 14px; }
.filter-select { padding: 6px 12px; border: 1px solid #e0dbd0; border-radius: var(--radius); font-size: 12px; color: var(--color-text-secondary); outline: none; }
.data-table { width: 100%; border-collapse: collapse; font-size: 12px; }
.data-table th { text-align: left; padding: 10px 12px; color: var(--color-text-secondary); font-weight: 500; border-bottom: 2px solid var(--color-border); }
.data-table td { padding: 10px 12px; border-bottom: 1px solid var(--color-accent-light); }
.cell-content { max-width: 200px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.status-tag { padding: 2px 8px; border-radius: 2px; font-size: 11px; }
.status-tag.pending { background: #fff3e0; color: #c08840; }
.status-tag.deleted { background: #ffebee; color: #c04040; }
.status-tag.dismissed { background: #e8f5e9; color: #5b8c5a; }
.cell-actions a { cursor: pointer; color: var(--color-text-secondary); }
.cell-actions a:hover { color: var(--color-primary); }
.cell-actions a:first-child:hover { color: var(--color-danger); }
.sep { color: var(--color-border); margin: 0 6px; }
.cell-muted { color: var(--color-text-muted); }
.empty-text { text-align: center; padding: 40px; color: var(--color-text-muted); font-size: 13px; }
.pagination { text-align: center; margin-top: 20px; display: flex; justify-content: center; gap: 12px; }
.pagination button { padding: 6px 14px; background: var(--color-bg, #fafaf7); color: var(--color-text-secondary); border: 1px solid #e0dbd0; border-radius: var(--radius); font-size: 12px; cursor: pointer; }
.pagination button:disabled { color: #d0c8b4; cursor: not-allowed; }
.page-info { font-size: 13px; color: var(--color-text-muted); }
</style>
