<template>
  <div class="notes-page">
    <div class="notes-container">
      <h1 class="notes-title">书余</h1>

      <div class="notes-tabs">
        <button
          :class="['tab-btn', { active: activeTab === 'siyu' }]"
          @click="switchTab('siyu')"
        >思余</button>
        <button
          :class="['tab-btn', { active: activeTab === 'yuyin' }]"
          @click="switchTab('yuyin')"
        >余音</button>
      </div>

      <!-- 思余 Tab -->
      <div v-if="activeTab === 'siyu'" class="siyu-tab">
        <div class="siyu-filter">
          <select v-model="noteType" @change="onTypeFilterChange">
            <option value="">全部类型</option>
            <option value="QUESTION">疑问</option>
            <option value="INSIGHT">心得</option>
          </select>
        </div>

        <div v-if="loading" class="loading-text">加载中...</div>
        <div v-else-if="myNotes.length === 0" class="empty-text">
          暂无手记。去读一本书，留下你的第一个书余吧。
        </div>
        <div v-else class="siyu-list">
          <div
            v-for="note in myNotes"
            :key="note.id"
            class="note-card"
          >
            <div class="note-header">
              <span class="note-book" @click="$router.push(`/book/${note.bookId}`)">
                《{{ note.bookTitle }}》
              </span>
              <span :class="['note-type-tag', note.type === 'QUESTION' ? 'tag-question' : 'tag-insight']">
                {{ note.type === 'QUESTION' ? '疑问' : '心得' }}
              </span>
              <span v-if="note.published" class="note-published-badge">已入余音</span>
            </div>
            <blockquote v-if="note.selectedText" class="note-quote">
              "{{ note.selectedText }}"
            </blockquote>
            <p class="note-content">{{ note.content }}</p>
            <div class="note-footer">
              <span class="note-time">{{ formatDate(note.createdAt) }}</span>
              <div class="note-actions">
                <button
                  v-if="!note.published"
                  class="btn-publish"
                  @click="handlePublish(note.id)"
                >送入余音</button>
                <button
                  v-else
                  class="btn-unpublish"
                  @click="handleUnpublish(note.id)"
                >撤回</button>
                <button class="btn-edit" @click="showEditDialog(note)">编辑</button>
                <button class="btn-delete" @click="handleDelete(note.id)">删除</button>
              </div>
            </div>
          </div>
        </div>
        <Pagination
          v-if="myTotal > 0"
          :page="myPage" :total="myTotal" :size="20"
          @change="(p) => { myPage = p; fetchMyNotes() }"
        />
      </div>

      <!-- 余音 Tab -->
      <div v-if="activeTab === 'yuyin'" class="yuyin-tab">
        <div v-if="loading" class="loading-text">加载中...</div>
        <div v-else-if="publicNotes.length === 0" class="empty-text">
          余音广场暂无内容。
        </div>
        <div v-else class="yuyin-list">
          <div
            v-for="note in publicNotes"
            :key="note.id"
            class="note-card public"
          >
            <div class="note-header">
              <span class="note-user" @click="viewProfile(note.userId)">{{ note.username }}</span>
              <span class="note-book" @click="$router.push(`/book/${note.bookId}`)">
                于《{{ note.bookTitle }}》
              </span>
              <span :class="['note-type-tag', note.type === 'QUESTION' ? 'tag-question' : 'tag-insight']">
                {{ note.type === 'QUESTION' ? '疑问' : '心得' }}
              </span>
            </div>
            <blockquote v-if="note.selectedText" class="note-quote">
              "{{ note.selectedText }}"
            </blockquote>
            <p class="note-content">{{ note.content }}</p>
            <div class="note-footer">
              <span class="note-time">{{ formatDate(note.createdAt) }}</span>
              <div class="note-actions">
                <button
                  :class="['btn-like', { liked: note.liked }]"
                  @click="handleLike(note)"
                >{{ note.liked ? '已赞' : '点赞' }} {{ note.likeCount > 0 ? note.likeCount : '' }}</button>
                <button class="btn-reply" @click="showReplyDialog(note)">回复</button>
                <button class="btn-report" @click="showReportDialog(note)">举报</button>
              </div>
            </div>
            <!-- 回复列表 -->
            <div v-if="note.replies && note.replies.length > 0" class="replies-section">
              <div
                v-for="reply in note.replies"
                :key="reply.id"
                class="reply-item"
              >
                <span class="reply-user">{{ reply.username }}</span>
                <span class="reply-content">{{ reply.content }}</span>
                <span class="reply-time">{{ formatDate(reply.createdAt) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 编辑对话框 -->
    <div v-if="editingNote" class="dialog-overlay" @click.self="editingNote = null">
      <div class="dialog-box">
        <h3>编辑手记</h3>
        <textarea v-model="editContent" rows="4" class="dialog-textarea"></textarea>
        <div class="dialog-actions">
          <button class="btn-cancel" @click="editingNote = null">取消</button>
          <button class="btn-save" @click="handleSaveEdit">保存</button>
        </div>
      </div>
    </div>

    <!-- 回复对话框 -->
    <div v-if="replyingNote" class="dialog-overlay" @click.self="replyingNote = null">
      <div class="dialog-box">
        <h3>回复手记</h3>
        <p class="dialog-context">{{ replyingNote.username }}：{{ replyingNote.content.slice(0, 50) }}{{ replyingNote.content.length > 50 ? '...' : '' }}</p>
        <textarea v-model="replyContent" rows="3" class="dialog-textarea" placeholder="写下你的回复..."></textarea>
        <div class="dialog-actions">
          <button class="btn-cancel" @click="replyingNote = null">取消</button>
          <button class="btn-save" @click="handleReply">发送</button>
        </div>
      </div>
    </div>

    <!-- 举报对话框（内联，不复用 ReportDialog 因为它耦合了 reviewId + reportReview） -->
    <div v-if="reportTarget" class="dialog-overlay" @click.self="reportTarget = null">
      <div class="dialog-box">
        <h3>举报手记</h3>
        <div class="report-reason-list">
          <label
            v-for="opt in reportReasonOptions"
            :key="opt.value"
            class="report-reason-item"
            :class="{ selected: reportReason === opt.value }"
          >
            <input type="radio" :value="opt.value" v-model="reportReason" class="report-radio" />
            <span>{{ opt.label }}</span>
          </label>
        </div>
        <div v-if="reportReason === 'other'" class="report-detail-area">
          <textarea v-model="reportDetail" class="dialog-textarea" placeholder="请描述具体理由（必填，200字以内）" maxlength="200" rows="3"></textarea>
        </div>
        <div class="dialog-actions">
          <button class="btn-cancel" @click="reportTarget = null">取消</button>
          <button class="btn-save" :disabled="!canReport || reportSubmitting" @click="handleReport">
            {{ reportSubmitting ? '提交中...' : '提交举报' }}
          </button>
        </div>
      </div>
    </div>

    <UserProfileDialog
      v-if="profileUserId"
      :user-id="profileUserId"
      :visible="showProfile"
      @close="showProfile = false"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

import Pagination from '../components/Pagination.vue'
import UserProfileDialog from '../components/UserProfileDialog.vue'
import { getMyNotes, getPublicNotes, publishNote, unpublishNote, updateNote, deleteNote, replyNote, toggleLikeNote } from '../api/note'
import { reportNote } from '../api/report'
import { formatDate } from '../utils/date'

const activeTab = ref('siyu')
const loading = ref(false)
const myNotes = ref([])
const myPage = ref(1)
const myTotal = ref(0)
const noteType = ref('')
const publicNotes = ref([])

// 编辑
const editingNote = ref(null)
const editContent = ref('')

// 回复
const replyingNote = ref(null)
const replyContent = ref('')

// 举报
const reportTarget = ref(null)
const reportReason = ref('')
const reportDetail = ref('')
const reportSubmitting = ref(false)

const reportReasonOptions = [
  { value: 'spam', label: '垃圾广告' },
  { value: 'abuse', label: '人身攻击' },
  { value: 'fake', label: '虚假信息' },
  { value: 'violation', label: '违规内容' },
  { value: 'other', label: '其他' }
]

const canReport = computed(() => {
  if (!reportReason.value) return false
  if (reportReason.value === 'other' && !reportDetail.value.trim()) return false
  return true
})

// 用户主页
const showProfile = ref(false)
const profileUserId = ref(null)

onMounted(() => {
  fetchMyNotes()
})

function switchTab(tab) {
  activeTab.value = tab
  loading.value = false
  if (tab === 'siyu') fetchMyNotes()
  else fetchPublicNotes()
}

async function fetchMyNotes() {
  loading.value = true
  try {
    const params = { page: myPage.value, size: 20 }
    if (noteType.value) params.type = noteType.value
    const res = await getMyNotes(params)
    myNotes.value = res.records
    myTotal.value = res.total
  } catch (e) {
    console.error('获取思余失败:', e)
  } finally {
    loading.value = false
  }
}

function onTypeFilterChange() {
  myPage.value = 1
  fetchMyNotes()
}

async function fetchPublicNotes() {
  loading.value = true
  try {
    const res = await getPublicNotes({ page: 1, size: 20 })
    publicNotes.value = res.records
  } catch (e) {
    console.error('获取余音失败:', e)
  } finally {
    loading.value = false
  }
}

async function handlePublish(noteId) {
  try {
    await publishNote(noteId)
    fetchMyNotes()
  } catch (e) {
    const msg = e?.response?.data?.message || '投递失败'
    alert(msg)
  }
}

async function handleUnpublish(noteId) {
  if (!confirm('确定要撤回这条手记吗？撤回后余音广场将不再显示。')) return
  try {
    await unpublishNote(noteId)
    fetchMyNotes()
  } catch (e) {
    const msg = e?.response?.data?.message || '撤回失败'
    alert(msg)
  }
}

async function handleDelete(noteId) {
  if (!confirm('确定要删除这条手记吗？')) return
  try {
    await deleteNote(noteId)
    fetchMyNotes()
  } catch (e) {
    const msg = e?.response?.data?.message || '删除失败'
    alert(msg)
  }
}

function showEditDialog(note) {
  editingNote.value = note
  editContent.value = note.content
}

async function handleSaveEdit() {
  if (!editContent.value.trim()) return
  try {
    await updateNote(editingNote.value.id, { content: editContent.value.trim() })
    editingNote.value = null
    fetchMyNotes()
  } catch (e) {
    const msg = e?.response?.data?.message || '编辑失败'
    alert(msg)
  }
}

function showReplyDialog(note) {
  replyingNote.value = note
  replyContent.value = ''
}

async function handleReply() {
  if (!replyContent.value.trim()) return
  try {
    await replyNote(replyingNote.value.rootId || replyingNote.value.id, {
      content: replyContent.value.trim()
    })
    replyingNote.value = null
    fetchPublicNotes()
  } catch (e) {
    const msg = e?.response?.data?.message || '回复失败'
    alert(msg)
  }
}

async function handleLike(note) {
  try {
    const res = await toggleLikeNote(note.id)
    note.liked = res.liked !== undefined ? res.liked : !note.liked
    note.likeCount = res.likeCount !== undefined ? res.likeCount : (note.likeCount + (note.liked ? 1 : -1))
  } catch (e) {
    const msg = e?.response?.data?.message || '操作失败'
    alert(msg)
  }
}

function showReportDialog(note) {
  reportTarget.value = note
  reportReason.value = ''
  reportDetail.value = ''
}

async function handleReport() {
  if (!canReport.value) return
  reportSubmitting.value = true
  try {
    await reportNote(reportTarget.value.id, {
      reason: reportReason.value,
      detail: reportReason.value === 'other' ? reportDetail.value.trim() : undefined
    })
    reportTarget.value = null
    alert('举报已提交')
  } catch (e) {
    const msg = e?.response?.data?.message || '举报失败'
    alert(msg)
  } finally {
    reportSubmitting.value = false
  }
}

function viewProfile(userId) {
  profileUserId.value = userId
  showProfile.value = true
}
</script>

<style scoped>
.notes-page {
  min-height: 100vh;
  background: var(--color-bg, #fafaf7);
}
.notes-container {
  max-width: 720px;
  margin: 0 auto;
  padding: 32px 24px 64px;
}
.notes-title {
  font-family: var(--font-serif);
  font-size: 28px;
  color: var(--color-text, #4a3d2f);
  text-align: center;
  margin-bottom: 24px;
  letter-spacing: 6px;
}
.notes-tabs {
  display: flex;
  justify-content: center;
  gap: 0;
  margin-bottom: 28px;
  border-bottom: 1px solid var(--color-border, #e8e4dc);
}
.tab-btn {
  padding: 8px 32px;
  border: none;
  background: none;
  font-size: 15px;
  color: var(--color-text-secondary, #8b8070);
  cursor: pointer;
  font-family: var(--font-serif);
  border-bottom: 2px solid transparent;
  transition: all 0.2s;
}
.tab-btn.active {
  color: var(--color-primary, #c9a96e);
  border-bottom-color: var(--color-primary, #c9a96e);
}
.tab-btn:hover { color: var(--color-text, #4a3d2f); }

.siyu-filter {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 16px;
}
.siyu-filter select {
  padding: 4px 12px;
  border: 1px solid var(--color-border, #e8e4dc);
  border-radius: 6px;
  font-size: 13px;
  color: var(--color-text, #4a3d2f);
  background: #fff;
}

.loading-text, .empty-text {
  text-align: center;
  padding: 40px;
  color: var(--color-text-muted, #a09880);
  font-size: 14px;
}

.note-card {
  background: #fff;
  border: 1px solid var(--color-border, #e8e4dc);
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
}
.note-card.public { border-left: 3px solid var(--color-primary, #c9a96e); }

.note-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  flex-wrap: wrap;
}
.note-user {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text, #4a3d2f);
  cursor: pointer;
}
.note-user:hover { color: var(--color-primary, #c9a96e); }
.note-book {
  font-size: 13px;
  color: var(--color-primary, #c9a96e);
  cursor: pointer;
}
.note-book:hover { text-decoration: underline; }

.note-type-tag {
  font-size: 11px;
  padding: 1px 8px;
  border-radius: 10px;
}
.tag-question { background: #e8f4fd; color: #3a7db8; }
.tag-insight { background: #fef3e4; color: #b8860b; }

.note-published-badge {
  font-size: 11px;
  padding: 1px 8px;
  border-radius: 10px;
  background: #e8f5e9;
  color: #388e3c;
  margin-left: auto;
}

.note-quote {
  font-size: 13px;
  color: var(--color-text-muted, #a09880);
  border-left: 2px solid #e0dbd0;
  padding-left: 10px;
  margin: 8px 0;
  font-style: italic;
}

.note-content {
  font-size: 14px;
  line-height: 1.7;
  color: var(--color-text, #4a3d2f);
  margin-bottom: 10px;
}

.note-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.note-time {
  font-size: 12px;
  color: var(--color-text-muted, #a09880);
}
.note-actions {
  display: flex;
  gap: 8px;
}
.note-actions button {
  font-size: 12px;
  padding: 2px 10px;
  border-radius: 4px;
  border: 1px solid #e0dbd0;
  background: #fff;
  color: var(--color-text-secondary, #8b8070);
  cursor: pointer;
  transition: all 0.15s;
}
.note-actions button:hover {
  border-color: var(--color-primary, #c9a96e);
  color: var(--color-primary, #c9a96e);
}
.btn-publish {
  color: var(--color-primary, #c9a96e) !important;
  border-color: var(--color-primary, #c9a96e) !important;
}
.btn-unpublish {
  color: #e8a838 !important;
  border-color: #e8a838 !important;
}
.btn-unpublish:hover {
  background: #fef9f0 !important;
}
.btn-like.liked {
  background: var(--color-primary, #c9a96e) !important;
  color: #fff !important;
  border-color: transparent !important;
}
.btn-delete:hover { color: #c04040 !important; border-color: #c04040 !important; }

/* 回复区域 */
.replies-section {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f0ebe0;
}
.reply-item {
  padding: 6px 0;
  font-size: 13px;
}
.reply-user {
  font-weight: 600;
  color: var(--color-text, #4a3d2f);
  margin-right: 8px;
}
.reply-content {
  color: var(--color-text-secondary, #8b8070);
}
.reply-time {
  font-size: 11px;
  color: var(--color-text-muted, #a09880);
  margin-left: 8px;
}

/* 举报原因列表 */
.report-reason-list {
  display: flex;
  flex-direction: column;
  gap: 2px;
  margin-bottom: 8px;
}
.report-reason-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.2s;
  font-size: 13px;
  color: var(--color-text, #4a3d2f);
}
.report-reason-item:hover { background: #f0ebe0; }
.report-reason-item.selected { background: #f0ebe0; }
.report-radio { accent-color: var(--color-primary, #c9a96e); }
.report-detail-area { margin-top: 8px; }

/* 对话框 */
.dialog-overlay {
  position: fixed; inset: 0;
  background: rgba(0,0,0,0.3);
  display: flex; align-items: center; justify-content: center;
  z-index: 100;
}
.dialog-box {
  background: #fff;
  border-radius: 10px;
  padding: 24px;
  width: 90%;
  max-width: 420px;
}
.dialog-box h3 {
  font-size: 16px;
  margin-bottom: 12px;
  color: var(--color-text, #4a3d2f);
}
.dialog-context {
  font-size: 12px;
  color: var(--color-text-muted, #a09880);
  margin-bottom: 10px;
  padding: 8px;
  background: #fafaf7;
  border-radius: 4px;
}
.dialog-textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #e0dbd0;
  border-radius: 6px;
  font-size: 14px;
  resize: vertical;
  font-family: var(--font-sans);
  color: var(--color-text, #4a3d2f);
}
.dialog-textarea:focus { outline: none; border-color: var(--color-primary, #c9a96e); }
.dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 14px;
}
.dialog-actions button {
  padding: 6px 20px;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
}
.btn-cancel {
  background: #f5f5f5;
  border: 1px solid #e0dbd0;
  color: var(--color-text-secondary, #8b8070);
}
.btn-save {
  background: var(--color-primary, #c9a96e);
  border: none;
  color: #fff;
}
.btn-save:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
