<template>
  <div class="notes-page" :class="{ 'yuyin-active': activeTab === 'yuyin' }">
    <div class="notes-container">
      <h1 class="notes-title">书余</h1>

      <div class="notes-tabs">
        <button :class="['tab-btn', { active: activeTab === 'siyu' }]" @click="switchTab('siyu')">思余</button>
        <button :class="['tab-btn', { active: activeTab === 'yuyin' }]" @click="switchTab('yuyin')">余音</button>
      </div>

      <!-- ============================================================ -->
      <!-- 思余 Tab（不变）                                                 -->
      <!-- ============================================================ -->
      <div v-if="activeTab === 'siyu'" class="siyu-tab">
        <div class="siyu-filter">
          <select v-model="noteType" @change="onTypeFilterChange">
            <option value="">全部类型</option>
            <option value="QUESTION">疑问</option>
            <option value="INSIGHT">心得</option>
          </select>
        </div>
        <div v-if="loading" class="loading-text">加载中...</div>
        <div v-else-if="myNotes.length === 0" class="empty-text">暂无手记。去读一本书，留下你的第一个书余吧。</div>
        <div v-else class="siyu-list">
          <div v-for="note in myNotes" :key="note.id" class="note-card">
            <div class="note-header">
              <span class="note-book" @click="$router.push(`/book/${note.bookId}`)">《{{ note.bookTitle }}》</span>
              <span :class="['note-type-tag', note.type === 'QUESTION' ? 'tag-question' : 'tag-insight']">{{ note.type === 'QUESTION' ? '疑问' : '心得' }}</span>
              <span v-if="note.published" class="note-published-badge">已入余音</span>
            </div>
            <blockquote v-if="note.selectedText" class="note-quote">"{{ note.selectedText }}"</blockquote>
            <p class="note-content">{{ note.content }}</p>
            <div class="note-footer">
              <span class="note-time">{{ formatDate(note.createdAt) }}</span>
              <div class="note-actions">
                <button v-if="!note.published" class="btn-publish" @click="handlePublish(note.id)">送入余音</button>
                <button v-else class="btn-unpublish" @click="handleUnpublish(note.id)">撤回</button>
                <button class="btn-edit" @click="showEditDialog(note)">编辑</button>
                <button class="btn-delete" @click="handleDelete(note.id)">删除</button>
              </div>
            </div>
          </div>
        </div>
        <Pagination v-if="myTotal > 0" :page="myPage" :total="myTotal" :size="20" @change="(p) => { myPage = p; fetchMyNotes() }" />
      </div>

      <!-- ============================================================ -->
      <!-- 余音 Tab — 星图拖拽界面                                           -->
      <!-- ============================================================ -->
      <div v-if="activeTab === 'yuyin'" class="yuyin-star-viewport" ref="viewportRef"
        @mousedown="onViewportMouseDown"
      >
        <!-- 节点画布 -->
        <div class="yuyin-canvas" ref="canvasRef" :style="canvasStyle">
          <!-- 连线 SVG -->
          <svg class="yuyin-lines" ref="linesRef"
            :width="CANVAS_W" :height="CANVAS_H"
            :style="{ position: 'absolute', top: 0, left: 0, pointerEvents: 'none' }"
          >
            <line
              v-for="line in visibleLinks"
              :key="line.key"
              :x1="line.x1" :y1="line.y1" :x2="line.x2" :y2="line.y2"
              :stroke-opacity="line.opacity"
              stroke="var(--color-primary, #c9a96e)"
              stroke-width="0.5"
            />
          </svg>

          <!-- 用户中心节点 -->
          <div
            class="yuyin-center"
            :class="{ ready: centerReady }"
            :style="{ left: centerX + 'px', top: centerY + 'px' }"
          >
            <div class="center-avatar">{{ userInitial }}</div>
            <div class="center-name">{{ username }}</div>
            <div class="center-stat">共 {{ myPublishedCount }} 篇书余</div>
          </div>

          <!-- 他人书余节点 -->
          <div
            v-for="(node, i) in visibleNodes"
            :key="node.note.id"
            class="yuyin-node"
            :class="{ entered: node.entered, selected: selectedNode === node }"
            :style="nodeStyle(node)"
            @mouseenter="onNodeEnter(node)"
            @mouseleave="onNodeLeave"
            @click.stop="onNodeClick(node)"
          >
            <blockquote class="node-quote">"{{ node.note.selectedText || node.note.content.slice(0, 40) }}"</blockquote>
            <span class="node-author">{{ node.note.username }}</span>
            <span :class="['node-type', node.note.type === 'QUESTION' ? 'type-q' : 'type-i']">{{ node.note.type === 'QUESTION' ? '疑' : '悟' }}</span>
            <span class="node-book">《{{ node.note.bookTitle }}》</span>
          </div>
        </div>

        <!-- 详情浮卡 -->
        <div v-if="detailNote" class="yuyin-detail" :style="detailStyle" @mouseenter="detailHover = true" @mouseleave="detailHover = false">
          <div class="detail-header">
            <span class="detail-user" @click="viewProfile(detailNote.userId)">{{ detailNote.username }}</span>
            <span class="detail-book" @click="$router.push(`/book/${detailNote.bookId}`)">《{{ detailNote.bookTitle }}》</span>
            <span :class="['note-type-tag', detailNote.type === 'QUESTION' ? 'tag-question' : 'tag-insight']">{{ detailNote.type === 'QUESTION' ? '疑问' : '心得' }}</span>
          </div>
          <blockquote v-if="detailNote.selectedText" class="note-quote">"{{ detailNote.selectedText }}"</blockquote>
          <p class="detail-content">{{ detailNote.content }}</p>
          <div class="detail-actions">
            <button :class="['btn-like', { liked: detailNote.liked }]" @click="handleLike(detailNote)">{{ detailNote.liked ? '已赞' : '点赞' }} {{ detailNote.likeCount }}</button>
            <button class="btn-reply" @click="showReplyDialog(detailNote)">回复</button>
            <button class="btn-report" @click="showReportDialog(detailNote)">举报</button>
          </div>
        </div>

        <!-- 加载/空态 -->
        <div v-if="loadingYuyin" class="yuyin-loading">星光渐亮...</div>
        <div v-else-if="publicNotes.length === 0" class="yuyin-empty">余音广场暂无内容</div>
      </div>
    </div>

    <!-- 对话框（复用思余的） -->
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

    <div v-if="reportTarget" class="dialog-overlay" @click.self="reportTarget = null">
      <div class="dialog-box">
        <h3>举报手记</h3>
        <div class="report-reason-list">
          <label v-for="opt in reportReasonOptions" :key="opt.value" class="report-reason-item" :class="{ selected: reportReason === opt.value }">
            <input type="radio" :value="opt.value" v-model="reportReason" class="report-radio" />
            <span>{{ opt.label }}</span>
          </label>
        </div>
        <textarea v-if="reportReason === 'other'" v-model="reportDetail" rows="2" class="dialog-textarea" placeholder="请补充说明..."></textarea>
        <div class="dialog-actions">
          <button class="btn-cancel" @click="reportTarget = null">取消</button>
          <button class="btn-save" :disabled="!canReport" @click="handleReportSubmit">提交</button>
        </div>
      </div>
    </div>

    <UserProfileDialog v-if="profileUserId" :user-id="profileUserId" :visible="showProfile" @close="showProfile = false" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import Pagination from '../components/Pagination.vue'
import UserProfileDialog from '../components/UserProfileDialog.vue'
import { getMyNotes, getPublicNotes, publishNote, unpublishNote, updateNote, deleteNote, replyNote, toggleLikeNote } from '../api/note'
import { reportNote } from '../api/report'
import { formatDate } from '../utils/date'
import { getUserIdFromToken } from '../utils/jwt'

// ============================================================
// 通用
// ============================================================
const activeTab = ref('siyu')
const loading = ref(false)

// ============================================================
// 思余
// ============================================================
const myNotes = ref([])
const myPage = ref(1)
const myTotal = ref(0)
const noteType = ref('')
const editingNote = ref(null)
const editContent = ref('')
const myPublishedCount = ref(0)

async function fetchMyNotes() {
  loading.value = true
  try {
    const params = { page: myPage.value, size: 20 }
    if (noteType.value) params.type = noteType.value
    const res = await getMyNotes(params)
    myNotes.value = res.records
    myTotal.value = res.total
    myPublishedCount.value = res.records.filter(n => n.published).length
  } catch (e) {
    console.error('获取思余失败:', e)
  } finally {
    loading.value = false
  }
}

function onTypeFilterChange() { myPage.value = 1; fetchMyNotes() }
async function handlePublish(noteId) { try { await publishNote(noteId); fetchMyNotes() } catch (e) { alert(e?.response?.data?.message || '投递失败') } }
async function handleUnpublish(noteId) { if (!confirm('确定要撤回这条手记吗？')) return; try { await unpublishNote(noteId); fetchMyNotes() } catch (e) { alert(e?.response?.data?.message || '撤回失败') } }
async function handleDelete(noteId) { if (!confirm('确定要删除这条手记吗？')) return; try { await deleteNote(noteId); fetchMyNotes() } catch (e) { alert(e?.response?.data?.message || '删除失败') } }
function showEditDialog(note) { editingNote.value = note; editContent.value = note.content }
async function handleSaveEdit() { if (!editContent.value.trim()) return; try { await updateNote(editingNote.value.id, { content: editContent.value.trim() }); editingNote.value = null; fetchMyNotes() } catch (e) { alert(e?.response?.data?.message || '编辑失败') } }

// ============================================================
// 余音 — 星图
// ============================================================
const viewportRef = ref(null)
const canvasRef = ref(null)
const linesRef = ref(null)
const CANVAS_W = 3200
const CANVAS_H = 2400
const viewOffset = ref({ x: 0, y: 0 })
const canvasStyle = computed(() => ({ transform: `translate(${-viewOffset.value.x}px, ${-viewOffset.value.y}px)` }))

const loadingYuyin = ref(false)
const publicNotes = ref([])
const centerReady = ref(false)
const visibleNodes = ref([])
const visibleLinks = ref([])
const selectedNode = ref(null)
const detailNote = ref(null)
const detailHover = ref(false)
const detailStyle = ref({})
const centerX = ref(CANVAS_W / 2)
const centerY = ref(CANVAS_H / 2)

const username = computed(() => localStorage.getItem('username') || '读者')
const userInitial = computed(() => (username.value || '读')[0])
const currentUserId = computed(() => {
  const token = localStorage.getItem('token')
  if (!token) return null
  try { return getUserIdFromToken(token) } catch { return null }
})

// 回复/举报
const replyingNote = ref(null)
const replyContent = ref('')
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

// 伪随机定位
function nodePos(index, total) {
  const angle = (index / total) * Math.PI * 2 + (index * 0.618)
  const radius = 260 + (index * 37) % 200 + Math.sin(index * 1.7) * 80
  return {
    x: CANVAS_W / 2 + Math.cos(angle) * radius,
    y: CANVAS_H / 2 + Math.sin(angle) * radius * 0.7
  }
}

function nodeStyle(node) {
  return {
    left: node.x + 'px',
    top: node.y + 'px',
    zIndex: Math.round(node.y / 10),
    '--pop-delay': node.delay + 's'
  }
}

// 构建节点
let entryTimer = null
function buildStarMap() {
  visibleNodes.value = []
  visibleLinks.value = []
  centerReady.value = false
  selectedNode.value = null
  detailNote.value = null

  const total = publicNotes.value.length
  if (total === 0) return

  // 延迟显示中心
  setTimeout(() => { centerReady.value = true }, 300)

  // 逐一浮现节点
  const nodes = publicNotes.value.map((note, i) => {
    const pos = nodePos(i, total)
    return {
      note,
      x: pos.x,
      y: pos.y,
      entered: false,
      delay: 0.5 + i * 0.18
    }
  })

  nodes.forEach((node, i) => {
    setTimeout(() => {
      node.entered = true
      visibleNodes.value = [...nodes.filter(n => n.entered)]

      // 连线也逐步显现
      if (visibleLinks.value.length < visibleNodes.value.length) {
        const centerPos = { x: CANVAS_W / 2, y: CANVAS_H / 2 }
        visibleLinks.value = visibleNodes.value.map((n, j) => ({
          key: `link-${j}`,
          x1: centerPos.x, y1: centerPos.y,
          x2: n.x, y2: n.y,
          opacity: 0.15 + 0.5 * (j / total)
        }))
      }
    }, node.delay * 1000)
  })
}

async function fetchPublicNotes() {
  loadingYuyin.value = true
  try {
    const res = await getPublicNotes({ page: 1, size: 20 })
    // 过滤掉自己的手记
    publicNotes.value = (res.records || []).filter(n => n.userId !== currentUserId.value)
    await nextTick()
    centerOnViewport()
    buildStarMap()
  } catch (e) {
    console.error('获取余音失败:', e)
  } finally {
    loadingYuyin.value = false
  }
}

// 初始化视口居中
function centerOnViewport() {
  const vw = viewportRef.value?.clientWidth || window.innerWidth
  const vh = viewportRef.value?.clientHeight || window.innerHeight
  viewOffset.value = {
    x: Math.max(0, (CANVAS_W - vw) / 2),
    y: Math.max(0, (CANVAS_H - vh) / 2)
  }
}

// ============================================================
// 拖拽平移
// ============================================================
let dragging = false, dsx, dsy, dox, doy
let velX = 0, velY = 0, lx, ly, lt, decelRaf = null

function onViewportMouseDown(e) {
  if (e.target.closest('.yuyin-node') || e.target.closest('.yuyin-detail') || e.target.closest('.yuyin-center')) return
  dragging = true
  cancelAnimationFrame(decelRaf)
  dsx = e.clientX; dsy = e.clientY
  dox = viewOffset.value.x; doy = viewOffset.value.y
  lx = e.clientX; ly = e.clientY; lt = Date.now()
}

window.addEventListener('mousemove', (e) => {
  if (!dragging) return
  viewOffset.value = {
    x: Math.max(0, Math.min(dox - (e.clientX - dsx), CANVAS_W - (viewportRef.value?.clientWidth || window.innerWidth))),
    y: Math.max(0, Math.min(doy - (e.clientY - dsy), CANVAS_H - (viewportRef.value?.clientHeight || window.innerHeight)))
  }
  const n = Date.now(), dt = n - lt
  if (dt > 0) { velX = (e.clientX - lx) / dt * 16; velY = (e.clientY - ly) / dt * 16 }
  lx = e.clientX; ly = e.clientY; lt = n
})

window.addEventListener('mouseup', () => {
  if (!dragging) return
  dragging = false
  if (Math.abs(velX) > 0.5 || Math.abs(velY) > 0.5) decelerate()
})

function decelerate() {
  velX *= 0.92; velY *= 0.92
  const vw = viewportRef.value?.clientWidth || window.innerWidth
  const vh = viewportRef.value?.clientHeight || window.innerHeight
  viewOffset.value = {
    x: Math.max(0, Math.min(viewOffset.value.x - velX, CANVAS_W - vw)),
    y: Math.max(0, Math.min(viewOffset.value.y - velY, CANVAS_H - vh))
  }
  if (Math.abs(velX) > 0.1 || Math.abs(velY) > 0.1) {
    decelRaf = requestAnimationFrame(decelerate)
  }
}

// ============================================================
// 节点交互
// ============================================================
function onNodeEnter(node) {
  selectedNode.value = node
}

function onNodeLeave() {
  if (!detailHover.value) {
    selectedNode.value = null
  }
}

function onNodeClick(node) {
  detailNote.value = node.note
  detailHover.value = false
  const el = viewportRef.value
  if (!el) return
  const rect = el.getBoundingClientRect()
  // 卡片靠近点击位置，但保持在视口内
  const cardW = 340, cardH = 280
  let left = node.x - viewOffset.value.x - cardW / 2
  let top = node.y - viewOffset.value.y - cardH - 20
  if (left < 16) left = 16
  if (left + cardW > rect.width - 16) left = rect.width - cardW - 16
  if (top < 16) top = node.y - viewOffset.value.y + 20
  if (top + cardH > rect.height - 16) top = rect.height - cardH - 16
  detailStyle.value = { left: left + 'px', top: top + 'px' }
}

// ============================================================
// 余音操作（点赞/回复/举报/查看主页）
// ============================================================
async function handleLike(note) {
  try {
    const res = await toggleLikeNote(note.id)
    note.liked = res.liked !== undefined ? res.liked : !note.liked
    if (res.likeCount !== undefined) note.likeCount = res.likeCount
  } catch (e) { alert(e?.response?.data?.message || '操作失败') }
}

function showReplyDialog(note) { replyingNote.value = note; replyContent.value = '' }
async function handleReply() {
  if (!replyContent.value.trim()) return
  try { await replyNote(replyingNote.value.rootId || replyingNote.value.id, { content: replyContent.value.trim() }); replyingNote.value = null } catch (e) { alert(e?.response?.data?.message || '回复失败') }
}

function showReportDialog(note) { reportTarget.value = note; reportReason.value = ''; reportDetail.value = '' }
async function handleReportSubmit() {
  if (!canReport.value) return
  reportSubmitting.value = true
  try { await reportNote(reportTarget.value.id, { reason: reportReason.value, detail: reportDetail.value }); reportTarget.value = null; alert('举报已提交') } catch (e) { alert(e?.response?.data?.message || '举报失败') } finally { reportSubmitting.value = false }
}

function viewProfile(userId) { profileUserId.value = userId; showProfile.value = true }

// ============================================================
// 生命周期
// ============================================================
function switchTab(tab) {
  activeTab.value = tab
  if (tab === 'siyu') fetchMyNotes()
  else fetchPublicNotes()
}

onMounted(() => { fetchMyNotes() })
onBeforeUnmount(() => {
  if (entryTimer) clearTimeout(entryTimer)
  if (decelRaf) cancelAnimationFrame(decelRaf)
})
</script>

<style scoped>
/* ============================================================
   基础
   ============================================================ */
.notes-page { min-height: 100vh; background: var(--color-bg, #fafaf7); }
.notes-page.yuyin-active { background: #1c1814; }
.notes-container { max-width: 100%; margin: 0 auto; padding: 32px 24px 64px; }
.notes-title { font-family: var(--font-serif); font-size: 28px; color: var(--color-text, #4a3d2f); text-align: center; margin-bottom: 24px; letter-spacing: 6px; }
.yuyin-active .notes-title { color: rgba(255,255,255,0.7); }
.notes-tabs { display: flex; justify-content: center; gap: 0; margin-bottom: 28px; border-bottom: 1px solid var(--color-border, #e8e4dc); }
.yuyin-active .notes-tabs { border-bottom-color: rgba(255,255,255,0.08); }
.tab-btn { padding: 8px 32px; border: none; background: none; font-size: 15px; color: var(--color-text-secondary, #8b8070); cursor: pointer; font-family: var(--font-serif); border-bottom: 2px solid transparent; transition: all 0.2s; }
.yuyin-active .tab-btn { color: rgba(255,255,255,0.35); }
.tab-btn.active { color: var(--color-primary, #c9a96e); border-bottom-color: var(--color-primary, #c9a96e); }
.tab-btn:hover { color: var(--color-text, #4a3d2f); }
.yuyin-active .tab-btn:hover { color: rgba(255,255,255,0.7); }

/* ============================================================
   思余
   ============================================================ */
.siyu-tab { max-width: 720px; margin: 0 auto; }
.siyu-filter { display: flex; justify-content: flex-end; margin-bottom: 16px; }
.siyu-filter select { padding: 4px 12px; border: 1px solid var(--color-border, #e8e4dc); border-radius: 6px; font-size: 13px; color: var(--color-text, #4a3d2f); background: #fff; }
.loading-text, .empty-text { text-align: center; padding: 40px; color: var(--color-text-muted, #a09880); font-size: 14px; }
.note-card { background: #fff; border: 1px solid var(--color-border, #e8e4dc); border-radius: 8px; padding: 16px; margin-bottom: 12px; }
.note-header { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; flex-wrap: wrap; }
.note-book { font-size: 13px; color: var(--color-primary, #c9a96e); cursor: pointer; }
.note-book:hover { text-decoration: underline; }
.note-type-tag { font-size: 11px; padding: 1px 8px; border-radius: 10px; }
.tag-question { background: #e8f4fd; color: #3a7db8; }
.tag-insight { background: #fef3e4; color: #b8860b; }
.note-published-badge { font-size: 11px; padding: 1px 8px; border-radius: 10px; background: #e8f5e9; color: #388e3c; margin-left: auto; }
.note-quote { font-size: 13px; color: var(--color-text-muted, #a09880); border-left: 2px solid #e0dbd0; padding-left: 10px; margin: 8px 0; font-style: italic; }
.note-content { font-size: 14px; line-height: 1.7; color: var(--color-text, #4a3d2f); margin-bottom: 10px; }
.note-footer { display: flex; align-items: center; justify-content: space-between; }
.note-time { font-size: 12px; color: var(--color-text-muted, #a09880); }
.note-actions { display: flex; gap: 8px; }
.note-actions button { font-size: 12px; padding: 2px 10px; border-radius: 4px; border: 1px solid #e0dbd0; background: #fff; color: var(--color-text-secondary, #8b8070); cursor: pointer; transition: all 0.15s; }
.note-actions button:hover { border-color: var(--color-primary, #c9a96e); color: var(--color-primary, #c9a96e); }
.btn-publish { color: var(--color-primary, #c9a96e) !important; border-color: var(--color-primary, #c9a96e) !important; }
.btn-unpublish { color: #e8a838 !important; border-color: #e8a838 !important; }
.btn-unpublish:hover { background: #fef9f0 !important; }
.btn-like.liked { background: var(--color-primary, #c9a96e) !important; color: #fff !important; border-color: transparent !important; }
.btn-delete:hover { color: #c04040 !important; border-color: #c04040 !important; }

/* ============================================================
   余音 — 星图视口
   ============================================================ */
.yuyin-star-viewport {
  position: relative;
  width: 100%;
  height: calc(100vh - 160px);
  min-height: 500px;
  overflow: hidden;
  cursor: grab;
  background: radial-gradient(ellipse 60% 50% at 50% 40%, #2a2218 0%, #1c1814 40%, #0f0d0a 70%);
  border-radius: 12px;
  user-select: none;
  -webkit-user-select: none;
}
.yuyin-star-viewport:active { cursor: grabbing; }

.yuyin-canvas {
  position: absolute;
  width: 3200px;
  height: 2400px;
  transition: none;
}

/* 中心节点 — 用户自身 */
.yuyin-center {
  position: absolute;
  transform: translate(-50%, -50%);
  text-align: center;
  z-index: 100;
  opacity: 0;
  transition: opacity 1.2s ease;
}
.yuyin-center.ready { opacity: 1; }
.center-avatar {
  width: 72px; height: 72px;
  border-radius: 50%;
  background: var(--color-primary, #c9a96e);
  color: #fff;
  display: flex; align-items: center; justify-content: center;
  font-size: 28px; font-family: var(--font-serif);
  margin: 0 auto 10px;
  box-shadow: 0 0 28px rgba(201,169,110,0.4), 0 0 60px rgba(201,169,110,0.15);
}
.center-name {
  font-size: 14px;
  color: rgba(255,255,255,0.8);
  font-family: var(--font-serif);
  letter-spacing: 2px;
  margin-bottom: 4px;
}
.center-stat {
  font-size: 11px;
  color: rgba(255,255,255,0.4);
}

/* 他人书余节点 */
.yuyin-node {
  position: absolute;
  transform: translate(-50%, -50%) scale(0);
  max-width: 200px;
  padding: 12px 16px;
  background: rgba(40,32,22,0.92);
  border: 1px solid rgba(201,169,110,0.2);
  border-radius: 10px;
  cursor: pointer;
  transition: transform 0.35s cubic-bezier(0.34,1.56,0.64,1), box-shadow 0.35s ease;
}
.yuyin-node.entered { transform: translate(-50%, -50%) scale(1); }
.yuyin-node:hover, .yuyin-node.selected {
  z-index: 200 !important;
  border-color: var(--color-primary, #c9a96e);
  box-shadow: 0 0 22px rgba(201,169,110,0.25), 0 0 50px rgba(201,169,110,0.1);
}
.node-quote {
  font-size: 12px;
  color: rgba(255,255,255,0.55);
  font-style: italic;
  margin-bottom: 6px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.node-author {
  font-size: 11px;
  color: rgba(201,169,110,0.8);
  font-weight: 600;
}
.node-type {
  font-size: 10px;
  padding: 0px 6px;
  border-radius: 8px;
  margin-left: 6px;
}
.type-q { background: rgba(58,125,184,0.2); color: #5b9bd5; }
.type-i { background: rgba(184,134,11,0.2); color: #d4a017; }
.node-book {
  display: block;
  font-size: 10px;
  color: rgba(255,255,255,0.3);
  margin-top: 4px;
}

/* 详情浮卡 */
.yuyin-detail {
  position: absolute;
  z-index: 300;
  width: 340px;
  padding: 18px 20px;
  background: rgba(30,22,14,0.97);
  border: 1px solid rgba(201,169,110,0.25);
  border-radius: 14px;
  box-shadow: 0 8px 40px rgba(0,0,0,0.5);
}
.yuyin-detail .detail-header { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; margin-bottom: 10px; }
.yuyin-detail .detail-user { font-size: 13px; font-weight: 600; color: var(--color-primary, #c9a96e); cursor: pointer; }
.yuyin-detail .detail-user:hover { text-decoration: underline; }
.yuyin-detail .detail-book { font-size: 12px; color: rgba(255,255,255,0.5); cursor: pointer; }
.yuyin-detail .detail-book:hover { color: rgba(255,255,255,0.8); }
.yuyin-detail .detail-content { font-size: 13px; line-height: 1.7; color: rgba(255,255,255,0.7); margin-bottom: 12px; }
.yuyin-detail .detail-actions { display: flex; gap: 8px; }
.yuyin-detail .detail-actions button { font-size: 11px; padding: 3px 12px; border-radius: 4px; border: 1px solid rgba(255,255,255,0.15); background: rgba(255,255,255,0.05); color: rgba(255,255,255,0.55); cursor: pointer; transition: all 0.2s; }
.yuyin-detail .detail-actions button:hover { border-color: var(--color-primary, #c9a96e); color: var(--color-primary, #c9a96e); }

.yuyin-loading, .yuyin-empty {
  position: absolute; inset: 0;
  display: flex; align-items: center; justify-content: center;
  font-size: 14px; color: rgba(255,255,255,0.35);
  font-family: var(--font-serif); letter-spacing: 0.1em;
}

/* ============================================================
   对话框
   ============================================================ */
.dialog-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.3); display: flex; align-items: center; justify-content: center; z-index: 400; }
.dialog-box { background: #fff; border-radius: 10px; padding: 24px; width: 90%; max-width: 420px; }
.dialog-box h3 { font-size: 16px; margin-bottom: 12px; color: var(--color-text, #4a3d2f); }
.dialog-context { font-size: 12px; color: var(--color-text-muted, #a09880); margin-bottom: 10px; padding: 8px; background: #fafaf7; border-radius: 4px; }
.dialog-textarea { width: 100%; padding: 10px; border: 1px solid #e0dbd0; border-radius: 6px; font-size: 14px; resize: vertical; font-family: var(--font-sans); color: var(--color-text, #4a3d2f); }
.dialog-textarea:focus { outline: none; border-color: var(--color-primary, #c9a96e); }
.dialog-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 14px; }
.dialog-actions button { padding: 6px 20px; border-radius: 6px; font-size: 13px; cursor: pointer; }
.btn-cancel { background: #f5f5f5; border: 1px solid #e0dbd0; color: var(--color-text-secondary, #8b8070); }
.btn-save { background: var(--color-primary, #c9a96e); border: none; color: #fff; }
.btn-save:disabled { opacity: 0.5; cursor: not-allowed; }
.report-reason-list { display: flex; flex-direction: column; gap: 6px; margin-bottom: 10px; }
.report-reason-item { display: flex; align-items: center; gap: 8px; padding: 8px 12px; border: 1px solid #e0dbd0; border-radius: 6px; cursor: pointer; font-size: 13px; color: var(--color-text, #4a3d2f); transition: all 0.15s; }
.report-reason-item.selected { border-color: var(--color-primary, #c9a96e); background: rgba(201,169,110,0.06); }
.report-radio { accent-color: var(--color-primary, #c9a96e); }
</style>
