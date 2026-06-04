<template>
  <div v-if="visible" class="conv-overlay" @click.self="$emit('close')">
    <div class="conv-modal">
      <div class="conv-header">
        <span class="conv-title">完整对话</span>
        <span class="conv-close" @click="$emit('close')">&times;</span>
      </div>

      <div class="conv-body" ref="bodyRef">
        <div v-for="(msg, idx) in flatMessages" :key="msg.id" class="conv-msg">
          <div v-if="idx === 1" class="conv-root-divider"></div>
          <div class="conv-msg-header">
            <span class="conv-msg-user" @click.stop="$emit('view-user', msg.userId)">{{ msg.username }}</span>
            <span v-if="msg.userId === currentUserId" class="conv-me-tag">(我)</span>
            <span class="conv-msg-time">{{ formatDate(msg.createdAt) }}</span>
          </div>
          <div class="conv-msg-content">
            <span v-if="idx > 0 && msg.parentId" class="conv-reply-prefix">回复 @{{ getParentUsername(msg.parentId) }}：</span>
            {{ msg.content }}
          </div>
          <div class="conv-msg-actions">
            <span
              class="conv-action"
              :class="{ liked: msg.liked }"
              @click="$emit('like', msg.id)"
            >赞 {{ msg.likeCount }}</span>
            <span class="conv-action" @click="startReply(msg)">回复</span>
            <span v-if="msg.userId !== currentUserId" class="conv-action" @click="$emit('report', msg.id)">举报</span>
            <template v-if="msg.userId === currentUserId && canEdit(msg)">
              <span class="conv-action" @click="startEdit(msg)">编辑</span>
              <span class="conv-action conv-action-danger" @click="$emit('delete', msg.id)">删除</span>
            </template>
          </div>
          <div v-if="msg.updatedAt && msg.updatedAt !== msg.createdAt" class="conv-edited-tag">(已编辑)</div>

          <div v-if="editingId === msg.id" class="conv-edit-area">
            <textarea v-model="editContent" class="conv-edit-input" rows="2"></textarea>
            <div class="conv-edit-actions">
              <button class="btn-cancel" @click="editingId = null">取消</button>
              <button class="btn-save" @click="doEdit(msg.id)">保存</button>
            </div>
          </div>

          <div v-if="replyingTo === msg.id" class="conv-reply-area">
            <div class="reply-target-tag">
              <span class="reply-target-label">回复</span>
              <span class="reply-target-username" @click.stop="$emit('view-user', msg.userId)">@{{ msg.username }}</span>
              <span class="reply-target-close" @click="replyingTo = null; replyContent = ''">&times;</span>
            </div>
            <textarea v-model="replyContent" class="conv-reply-input" rows="2" placeholder="写下你的回复..."></textarea>
            <div class="conv-reply-actions">
              <button class="btn-cancel" @click="replyingTo = null; replyContent = ''">取消</button>
              <button class="btn-save" @click="doReply(msg.id)">发送回复</button>
            </div>
          </div>
        </div>
      </div>

      <div v-if="!isLoggedIn" class="conv-footer-login">
        请<a href="#" @click.prevent="goLogin">登录</a>后参与对话
      </div>
      <div v-else-if="!replyingTo" class="conv-footer">
        <textarea v-model="newReplyContent" class="conv-footer-input" rows="2" placeholder="写下你的回复..."></textarea>
        <div class="conv-footer-actions">
          <button class="btn-save" :disabled="!newReplyContent.trim()" @click="doNewReply">发送回复</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps({
  visible: { type: Boolean, default: false },
  rootReview: { type: Object, required: true },
  threadReply: { type: Object, required: true },
  bookId: { type: Number, required: true },
  currentUserId: { type: Number, default: null }
})

const emit = defineEmits(['close', 'reply', 'like', 'delete', 'edit', 'report', 'view-user'])
const router = useRouter()

const bodyRef = ref(null)
const replyingTo = ref(null)
const replyContent = ref('')
const newReplyContent = ref('')
const editingId = ref(null)
const editContent = ref('')

const isLoggedIn = computed(() => props.currentUserId != null)

function flattenThread(root, threadStart) {
  const result = [root]
  if (!threadStart) return result

  function collect(reply) {
    result.push(reply)
    if (reply.replies) {
      reply.replies.forEach(collect)
    }
  }

  const children = root.replies || []
  for (const reply of children) {
    if (reply.id === threadStart.id) {
      collect(reply)
      break
    }
  }
  return result
}

const flatMessages = computed(() =>
  flattenThread(props.rootReview, props.threadReply)
)

const usernameMap = computed(() => {
  const map = {}
  for (const m of flatMessages.value) {
    map[m.id] = m.username
  }
  return map
})

function getParentUsername(parentId) {
  return usernameMap.value[parentId] || '未知用户'
}

watch(() => props.visible, async (v) => {
  if (v) {
    replyingTo.value = null
    replyContent.value = ''
    newReplyContent.value = ''
    editingId.value = null
    editContent.value = ''
    await nextTick()
    if (bodyRef.value) {
      bodyRef.value.scrollTop = bodyRef.value.scrollHeight
    }
  }
})

function formatDate(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 30) return `${days}天前`
  return date.toLocaleDateString('zh-CN')
}

function canEdit(msg) {
  if (!msg.createdAt) return false
  return (new Date() - new Date(msg.createdAt)) < 3 * 60 * 1000
}

function startReply(msg) {
  replyingTo.value = msg.id
  replyContent.value = ''
}

function doReply(parentId) {
  if (!replyContent.value.trim()) return
  emit('reply', parentId, replyContent.value.trim())
  replyContent.value = ''
  replyingTo.value = null
}

function doNewReply() {
  if (!newReplyContent.value.trim()) return
  emit('reply', props.threadReply.id, newReplyContent.value.trim())
  newReplyContent.value = ''
}

function startEdit(msg) {
  editContent.value = msg.content
  editingId.value = msg.id
}

function doEdit(id) {
  if (!editContent.value.trim()) return
  emit('edit', id, editContent.value.trim())
  editingId.value = null
}

function goLogin() {
  router.push('/login')
}
</script>

<style scoped>
.conv-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.35);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
}
.conv-modal {
  background: var(--color-card-bg, #fff);
  border-radius: 12px;
  width: 600px;
  max-width: 95vw;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
}
.conv-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid var(--color-border, #e8e4dc);
  flex-shrink: 0;
}
.conv-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text, #4a3d2f);
}
.conv-close {
  font-size: 22px;
  color: var(--color-text-muted, #a09880);
  cursor: pointer;
  line-height: 1;
}
.conv-close:hover { color: #4a3d2f; }
.conv-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px 20px;
}
.conv-root-divider {
  border-top: 1px dashed #e0dbd0;
  margin: 4px 0 14px;
}
.conv-msg {
  margin-bottom: 14px;
}
.conv-msg-header {
  font-size: 12px;
  margin-bottom: 2px;
}
.conv-msg-user {
  font-weight: 500;
  color: var(--color-text, #4a3d2f);
  cursor: pointer;
}
.conv-msg-user:hover { color: #c9a96e; }
.conv-me-tag {
  font-size: 11px;
  color: var(--color-text-muted, #a09880);
  margin-left: 2px;
}
.conv-msg-time {
  color: var(--color-text-muted, #a09880);
  margin-left: 8px;
}
.conv-msg-content {
  font-size: 13px;
  color: var(--color-text, #4a3d2f);
  line-height: 1.7;
  margin: 4px 0;
}
.conv-reply-prefix {
  color: var(--color-primary, #c9a96e);
  font-size: 12px;
}
.conv-msg-actions {
  display: flex;
  gap: 14px;
  font-size: 12px;
  color: var(--color-text-muted, #a09880);
  margin-top: 2px;
}
.conv-action {
  cursor: pointer;
  transition: color 0.2s;
}
.conv-action:hover { color: #c9a96e; }
.conv-action.liked {
  color: var(--color-primary, #c9a96e);
  font-weight: 600;
}
.conv-action-danger:hover { color: var(--color-danger, #c04040); }
.conv-edited-tag {
  font-size: 11px;
  color: var(--color-text-muted, #a09880);
  margin-top: 2px;
}
.conv-edit-area {
  margin-top: 8px;
}
.conv-edit-input {
  width: 100%;
  padding: 6px 10px;
  border: 1px solid #e0dbd0;
  border-radius: 6px;
  font-size: 13px;
  font-family: inherit;
  resize: vertical;
  outline: none;
}
.conv-edit-input:focus { border-color: var(--color-primary, #c9a96e); }
.conv-edit-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
  margin-top: 6px;
}
.conv-reply-area {
  margin-top: 8px;
}
.conv-reply-input {
  width: 100%;
  padding: 6px 10px;
  border: 1px solid #e0dbd0;
  border-radius: 6px;
  font-size: 12px;
  font-family: inherit;
  resize: vertical;
  outline: none;
}
.conv-reply-input:focus { border-color: var(--color-primary, #c9a96e); }
.conv-reply-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
  margin-top: 6px;
}
.conv-footer {
  padding: 12px 20px;
  border-top: 1px solid var(--color-border, #e8e4dc);
  flex-shrink: 0;
}
.conv-footer-input {
  width: 100%;
  padding: 8px 10px;
  border: 1px solid #e0dbd0;
  border-radius: 6px;
  font-size: 13px;
  font-family: inherit;
  resize: vertical;
  outline: none;
}
.conv-footer-input:focus { border-color: var(--color-primary, #c9a96e); }
.conv-footer-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 6px;
}
.conv-footer-login {
  padding: 12px 20px;
  border-top: 1px solid var(--color-border, #e8e4dc);
  text-align: center;
  font-size: 13px;
  color: var(--color-text-muted, #a09880);
  flex-shrink: 0;
}
.conv-footer-login a {
  color: var(--color-primary, #c9a96e);
  cursor: pointer;
}
.reply-target-tag {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 6px;
  font-size: 12px;
}
.reply-target-label { color: #8b8070; }
.reply-target-username {
  color: var(--color-primary, #c9a96e);
  font-weight: 500;
  cursor: pointer;
}
.reply-target-username:hover { text-decoration: underline; }
.reply-target-close {
  margin-left: auto;
  cursor: pointer;
  color: var(--color-text-muted, #a09880);
  font-size: 16px;
  line-height: 1;
}
.reply-target-close:hover { color: #8b8070; }
.btn-cancel {
  padding: 4px 12px;
  background: var(--color-bg, #fafaf7);
  color: var(--color-text-secondary, #8b8070);
  border: 1px solid #e0dbd0;
  border-radius: 6px;
  font-size: 12px;
  cursor: pointer;
}
.btn-save {
  padding: 4px 12px;
  background: var(--color-primary, #c9a96e);
  color: #fff;
  border: none;
  border-radius: 6px;
  font-size: 12px;
  cursor: pointer;
}
.btn-save:disabled { opacity: 0.5; cursor: not-allowed; }
</style>
