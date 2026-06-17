<template>
  <div class="inbox-page">
    <div class="inbox-header">
      <h2 class="inbox-title">收件箱</h2>
      <button v-if="totalCount > 0" class="btn-read-all" @click="onReadAll">全部已读</button>
    </div>
    <div v-if="loading" class="inbox-loading">加载中...</div>
    <div v-else-if="notifications.length === 0" class="inbox-empty">暂无通知</div>
    <div v-else class="inbox-list">
      <div v-for="n in notifications" :key="n.id" class="inbox-item" :class="{ unread: !n.read }" @click="onClick(n)">
        <div class="inbox-dot" v-if="!n.read"></div>
        <div class="inbox-body">
          <div class="inbox-item-title">{{ n.title }}</div>
          <div class="inbox-item-content">{{ n.content }}</div>
          <div class="inbox-item-time">{{ formatRelativeDate(n.createdAt) }}</div>
        </div>
      </div>
    </div>
    <div v-if="totalPages > 1" class="inbox-pagination">
      <button :disabled="page <= 1" @click="goPage(page - 1)">← 上一页</button>
      <span class="page-info">{{ page }} / {{ totalPages }}</span>
      <button :disabled="page >= totalPages" @click="goPage(page + 1)">下一页 →</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getNotifications, markAsRead, markAllAsRead } from '../api/report'
import { formatRelativeDate } from '../utils/date.js'

const router = useRouter()
const notifications = ref([])
const loading = ref(false)
const page = ref(1)
const totalCount = ref(0)
const pageSize = 20

const totalPages = computed(() => Math.ceil(totalCount.value / pageSize) || 0)
onMounted(() => fetchNotifications())

async function fetchNotifications() {
  loading.value = true
  try {
    const data = await getNotifications({ page: page.value, size: pageSize })
    notifications.value = data.records; totalCount.value = data.total
  } finally { loading.value = false }
}

async function onClick(n) {
  if (!n.read) { await markAsRead(n.id); n.read = true }
  if (n.noteId) {
    router.push('/notes')
  } else if (n.bookId && n.reviewId) {
    router.push({ path: `/book/${n.bookId}`, query: { reviewId: n.reviewId } })
  } else if (n.bookId) {
    router.push(`/book/${n.bookId}`)
  }
}
async function onReadAll() { await markAllAsRead(); notifications.value.forEach(n => n.read = true) }
function goPage(p) { page.value = p; fetchNotifications() }
</script>

<style scoped>
.inbox-page { padding: 24px 32px; max-width: 700px; }
.inbox-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
.inbox-title { font-size: 20px; font-family: var(--font-serif); color: var(--color-text, #4a3d2f); font-weight: 600; letter-spacing: 2px; }
.btn-read-all { padding: 5px 14px; background: var(--color-bg, #fafaf7); color: var(--color-text-secondary, #8b8070); border: 1px solid #e0dbd0; border-radius: var(--radius, 8px); font-size: 12px; cursor: pointer; }
.inbox-loading, .inbox-empty { text-align: center; padding: 60px; color: var(--color-text-muted, #a09880); font-size: 13px; }
.inbox-item { display: flex; gap: 10px; padding: 16px; background: var(--color-card-bg, #fff); border: 1px solid var(--color-card-border, #ece8df); border-radius: var(--radius, 8px); margin-bottom: 8px; cursor: pointer; transition: border-color 0.2s; }
.inbox-item:hover { border-color: var(--color-primary, #c9a96e); }
.inbox-item.unread { border-left: 3px solid var(--color-primary, #c9a96e); }
.inbox-dot { width: 8px; height: 8px; border-radius: 50%; background: var(--color-primary, #c9a96e); flex-shrink: 0; margin-top: 4px; }
.inbox-body { flex: 1; }
.inbox-item-title { font-size: 14px; font-weight: 500; color: var(--color-text, #4a3d2f); margin-bottom: 4px; }
.inbox-item-content { font-size: 12px; color: var(--color-text-secondary, #8b8070); line-height: 1.6; }
.inbox-item-time { font-size: 11px; color: var(--color-text-muted, #a09880); margin-top: 6px; }
.inbox-pagination { text-align: center; margin-top: 20px; display: flex; justify-content: center; gap: 12px; }
.inbox-pagination button { padding: 6px 14px; background: var(--color-bg, #fafaf7); color: var(--color-text-secondary, #8b8070); border: 1px solid #e0dbd0; border-radius: var(--radius, 8px); font-size: 12px; cursor: pointer; }
.inbox-pagination button:disabled { color: #d0c8b4; cursor: not-allowed; }
.page-info { font-size: 13px; color: var(--color-text-muted, #a09880); }
</style>
