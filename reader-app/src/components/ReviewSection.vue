<template>
  <div class="review-section">
    <div class="review-header-bar">
      <h3 class="review-title">书评</h3>
      <span v-if="totalCount > 0" class="review-count">({{ totalCount }})</span>
    </div>

    <div v-if="!isLoggedIn" class="review-login-hint">
      请<a href="#" @click.prevent="goLogin">登录</a>后发表书评
    </div>
    <div v-else class="review-post-area">
      <textarea
        v-model="postContent"
        class="review-post-input"
        rows="3"
        placeholder="写下你的书评..."
        maxlength="1000"
      ></textarea>
      <div class="review-post-footer">
        <span class="review-char-count">{{ postContent.length }}/1000</span>
        <button
          class="btn-post"
          :disabled="!postContent.trim() || posting"
          @click="postReview"
        >{{ posting ? '发表中...' : '发表书评' }}</button>
      </div>
    </div>

    <div v-if="totalCount > 0" class="review-sort">
      <span
        :class="{ active: sort === 'time' }"
        @click="changeSort('time')"
      >按时间</span>
      <span
        :class="{ active: sort === 'hot' }"
        @click="changeSort('hot')"
      >按热度</span>
    </div>

    <div v-if="loading" class="review-loading">加载中...</div>
    <div v-else-if="reviews.length === 0 && !posting" class="review-empty">暂无书评</div>
    <div v-else class="review-list">
      <ReviewItem
        v-for="review in reviews"
        :id="'review-' + review.id"
        :key="review.id"
        :review="review"
        :current-user-id="currentUserId"
        @like="onLike"
        @delete="onDelete"
        @edit="onEdit"
        @reply="onReply"
        @report="onReport"
        @view-user="onViewUser"
        @view-conversation="onViewConversation"
      />
    </div>

    <div v-if="totalPages > 1" class="review-pagination">
      <button :disabled="page <= 1" @click="goPage(page - 1)">← 上一页</button>
      <span class="page-info">{{ page }} / {{ totalPages }}</span>
      <button :disabled="page >= totalPages" @click="goPage(page + 1)">下一页 →</button>
    </div>
  </div>

  <ReportDialog
    v-if="reportTargetId"
    :review-id="reportTargetId"
    :visible="showReportDialog"
    @close="showReportDialog = false"
    @done="onReportDone"
  />
  <UserProfileDialog
    v-if="viewUserId"
    :user-id="viewUserId"
    :visible="showUserDialog"
    @close="showUserDialog = false"
  />
  <ConversationModal
    v-if="convRootReview && convThreadReply"
    :visible="convVisible"
    :root-review="convRootReview"
    :thread-reply="convThreadReply"
    :book-id="Number(bookId)"
    :current-user-id="currentUserId"
    @close="onConvClose"
    @reply="onConvReply"
    @like="onConvLike"
    @delete="onConvDelete"
    @edit="onConvEdit"
    @report="onConvReport"
    @view-user="onConvViewUser"
  />
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { getUserIdFromToken } from '../utils/jwt'
import ReviewItem from './ReviewItem.vue'
import ReportDialog from './ReportDialog.vue'
import UserProfileDialog from './UserProfileDialog.vue'
import ConversationModal from './ConversationModal.vue'
import {
  getReviews, createReview, createReply,
  updateReview, deleteReview, toggleLike
} from '../api/review'

const props = defineProps({
  bookId: { type: [Number, String], required: true },
  highlightReviewId: { type: Number, default: null }
})
const router = useRouter()

const reviews = ref([])
const loading = ref(false)
const posting = ref(false)
const sort = ref('time')
const page = ref(1)
const totalCount = ref(0)
const pageSize = 10
const postContent = ref('')
const reportTargetId = ref(null)
const showReportDialog = ref(false)
const viewUserId = ref(null)
const showUserDialog = ref(false)
const convVisible = ref(false)
const convRootReview = ref(null)
const convThreadReply = ref(null)

function onReport(reviewId) { reportTargetId.value = reviewId; showReportDialog.value = true }
function onReportDone() { showReportDialog.value = false; alert('举报已提交') }
function onViewUser(userId) { viewUserId.value = userId; showUserDialog.value = true }

function onViewConversation(rootReview, threadReply) {
  convRootReview.value = rootReview
  convThreadReply.value = threadReply
  convVisible.value = true
}

function onConvClose() {
  convVisible.value = false
  convRootReview.value = null
  convThreadReply.value = null
}

async function onConvReply(parentId, content) {
  await createReply(parentId, { content })
  await fetchReviews()
  const updatedRoot = reviews.value.find(r => r.id === convRootReview.value.id)
  if (updatedRoot) {
    convRootReview.value = updatedRoot
    const updatedThread = updatedRoot.replies.find(r => r.id === convThreadReply.value.id)
    if (updatedThread) {
      convThreadReply.value = updatedThread
    }
  }
}

async function onConvLike(reviewId) {
  await toggleLike(reviewId)
  toggleLikeLocal([convRootReview.value], reviewId)
}

async function onConvDelete(reviewId) {
  if (!confirm('确定删除这条评论吗？')) return
  await deleteReview(reviewId)
  await fetchReviews()
  const updatedRoot = reviews.value.find(r => r.id === convRootReview.value.id)
  if (updatedRoot) {
    convRootReview.value = updatedRoot
    const updatedThread = updatedRoot.replies.find(r => r.id === convThreadReply.value.id)
    if (updatedThread) {
      function countAll(reply) {
        if (!reply.replies || reply.replies.length === 0) return 0
        let c = reply.replies.length
        reply.replies.forEach(r => { c += countAll(r) })
        return c
      }
      const total = 2 + countAll(updatedThread)
      if (total < 6) {
        convVisible.value = false
      } else {
        convThreadReply.value = updatedThread
      }
    } else {
      convVisible.value = false
    }
  } else {
    convVisible.value = false
  }
}

async function onConvEdit(reviewId, content) {
  await updateReview(reviewId, { content })
  await fetchReviews()
  const updatedRoot = reviews.value.find(r => r.id === convRootReview.value.id)
  if (updatedRoot) {
    convRootReview.value = updatedRoot
    const updatedThread = updatedRoot.replies.find(r => r.id === convThreadReply.value.id)
    if (updatedThread) convThreadReply.value = updatedThread
  }
}

function onConvReport(reviewId) {
  convVisible.value = false
  onReport(reviewId)
}

function onConvViewUser(userId) {
  viewUserId.value = userId
  showUserDialog.value = true
}

const isLoggedIn = computed(() => !!localStorage.getItem('token'))
const currentUserId = computed(() => {
  const token = localStorage.getItem('token')
  if (!token) return null
  try {
    return getUserIdFromToken(token)
  } catch { return null }
})

const totalPages = computed(() => Math.ceil(totalCount.value / pageSize) || 0)

onMounted(() => fetchReviews())

async function fetchReviews() {
  loading.value = true
  try {
    const data = await getReviews(props.bookId, { sort: sort.value, page: page.value, size: pageSize })
    reviews.value = data.records
    totalCount.value = data.total
    if (props.highlightReviewId) {
      await nextTick()
      const el = document.getElementById('review-' + props.highlightReviewId)
      if (el) {
        el.scrollIntoView({ behavior: 'smooth', block: 'center' })
        el.classList.add('highlight-flash')
        setTimeout(() => el.classList.remove('highlight-flash'), 2000)
      }
    }
  } finally {
    loading.value = false
  }
}

async function postReview() {
  if (!postContent.value.trim()) return
  posting.value = true
  try {
    await createReview(props.bookId, { content: postContent.value.trim() })
    postContent.value = ''
    page.value = 1
    sort.value = 'time'
    await fetchReviews()
  } finally {
    posting.value = false
  }
}

async function onReply(reviewId, content) {
  await createReply(reviewId, { content })
  await fetchReviews()
}

async function onEdit(reviewId, content) {
  await updateReview(reviewId, { content })
  await fetchReviews()
}

async function onDelete(reviewId) {
  if (!confirm('确定删除这条书评吗？')) return
  await deleteReview(reviewId)
  await fetchReviews()
}

async function onLike(reviewId) {
  await toggleLike(reviewId)
  toggleLikeLocal(reviews.value, reviewId)
}

function toggleLikeLocal(list, id) {
  for (const r of list) {
    if (r.id === id) {
      r.liked = !r.liked
      r.likeCount += r.liked ? 1 : -1
      return true
    }
    if (r.replies && r.replies.length) {
      if (toggleLikeLocal(r.replies, id)) return true
    }
  }
  return false
}

function changeSort(newSort) {
  sort.value = newSort
  page.value = 1
  fetchReviews()
}

function goPage(p) {
  page.value = p
  fetchReviews()
  // scroll to review section top
  const el = document.querySelector('.review-section')
  if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

function goLogin() {
  router.push('/login')
}
</script>

<style scoped>
.review-section {
  margin-top: 24px;
  padding: 20px 0;
  border-top: 2px solid var(--color-primary, #c9a96e);
}
.review-header-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
}
.review-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text, #4a3d2f);
  font-family: var(--font-serif);
  letter-spacing: 2px;
  margin: 0;
}
.review-count {
  font-size: 13px;
  color: var(--color-text-muted, #a09880);
}
.review-login-hint {
  padding: 20px;
  text-align: center;
  font-size: 13px;
  color: var(--color-text-muted, #a09880);
  background: var(--color-bg, #fafaf7);
  border: 1px solid var(--color-border, #e8e4dc);
  border-radius: var(--radius, 8px);
}
.review-login-hint a {
  color: var(--color-primary, #c9a96e);
  cursor: pointer;
}
.review-post-area {
  margin-bottom: 20px;
  padding: 16px;
  background: var(--color-bg, #fafaf7);
  border: 1px solid var(--color-border, #e8e4dc);
  border-radius: var(--radius, 8px);
}
.review-post-input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #e0dbd0;
  border-radius: var(--radius-sm, 6px);
  font-size: 13px;
  resize: vertical;
  font-family: inherit;
  outline: none;
}
.review-post-input:focus { border-color: var(--color-primary, #c9a96e); }
.review-post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}
.review-char-count {
  font-size: 12px;
  color: var(--color-text-muted, #a09880);
}
.btn-post {
  padding: 7px 18px;
  background: var(--color-bg, #fafaf7);
  color: var(--color-text-secondary, #8b8070);
  border: 1px solid #e0dbd0;
  border-radius: var(--radius, 8px);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}
.btn-post:hover:not(:disabled) {
  border-color: var(--color-primary, #c9a96e);
  color: var(--color-primary, #c9a96e);
}
.btn-post:disabled { opacity: 0.5; cursor: not-allowed; }
.review-sort {
  display: flex;
  gap: 20px;
  border-bottom: 1px solid var(--color-border, #e8e4dc);
  padding-bottom: 10px;
  margin-bottom: 4px;
}
.review-sort span {
  font-size: 13px;
  color: var(--color-text-secondary, #8b8070);
  cursor: pointer;
  padding-bottom: 10px;
  margin-bottom: -11px;
  transition: color 0.2s;
}
.review-sort span.active {
  color: var(--color-primary, #c9a96e);
  font-weight: 600;
  border-bottom: 2px solid var(--color-primary, #c9a96e);
}
.review-loading, .review-empty {
  text-align: center;
  padding: 40px;
  font-size: 13px;
  color: var(--color-text-muted, #a09880);
}
.review-pagination {
  text-align: center;
  margin-top: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}
.review-pagination button {
  padding: 6px 14px;
  background: var(--color-bg, #fafaf7);
  color: var(--color-text-secondary, #8b8070);
  border: 1px solid #e0dbd0;
  border-radius: var(--radius, 8px);
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
}
.review-pagination button:hover:not(:disabled) {
  border-color: var(--color-primary, #c9a96e);
  color: var(--color-primary, #c9a96e);
}
.review-pagination button:disabled {
  color: #d0c8b4;
  cursor: not-allowed;
}
.page-info {
  font-size: 13px;
  color: var(--color-text-muted, #a09880);
}
:deep(.highlight-flash) {
  animation: highlightPulse 2s ease-out;
  border-radius: var(--radius, 8px);
}
@keyframes highlightPulse {
  0% { background: rgba(201, 169, 110, 0.3); }
  100% { background: transparent; }
}
</style>
