<template>
  <div :class="['review-item', { 'is-reply': isReply }]">
    <div class="review-body">
      <div class="review-header">
        <span class="review-username" @click.stop="$emit('view-user', review.userId)">{{ review.username }}</span>
        <span v-if="isOwn" class="review-me-tag">(我)</span>
      </div>
      <div class="review-content" v-if="!editing">{{ review.content }}</div>
      <div class="review-edit" v-else>
        <textarea v-model="editContent" class="review-edit-input" rows="3"></textarea>
        <div class="review-edit-actions">
          <button class="btn-cancel" @click="editing = false">取消</button>
          <button class="btn-save" @click="doEdit">保存</button>
        </div>
      </div>
      <div class="review-meta">
        <span>{{ formatDate(review.createdAt) }}</span>
        <span
          class="meta-action"
          :class="{ liked: review.liked }"
          @click="$emit('like', review.id)"
        >赞 {{ review.likeCount }}</span>
        <span class="meta-action" @click="showReplyInput = !showReplyInput">回复</span>
        <span v-if="!isOwn" class="meta-action" @click.stop="$emit('report', review.id)">举报</span>
        <template v-if="isOwn && canEdit">
          <span class="meta-action" @click="startEdit">编辑</span>
          <span class="meta-action meta-danger" @click="$emit('delete', review.id)">删除</span>
        </template>
      </div>
      <div v-if="review.updatedAt && review.updatedAt !== review.createdAt" class="review-edited-tag">
        (已编辑)
      </div>
    </div>

    <div v-if="showReplyInput" class="reply-input-area">
      <div class="reply-target-tag">
        <span class="reply-target-label">回复</span>
        <span class="reply-target-username" @click.stop="$emit('view-user', review.userId)">@{{ review.username }}</span>
        <span class="reply-target-close" @click="showReplyInput = false; replyContent = ''">&times;</span>
      </div>
      <textarea v-model="replyContent" class="reply-input" rows="2" placeholder="写下你的回复..."></textarea>
      <div class="reply-input-actions">
        <button class="btn-cancel" @click="showReplyInput = false; replyContent = ''">取消</button>
        <button class="btn-save" @click="doReply">回复</button>
      </div>
    </div>

    <div v-if="review.replies && review.replies.length" class="replies-list">
      <ReviewItem
        v-for="reply in displayReplies"
        :id="'review-' + reply.id"
        :key="reply.id"
        :review="reply"
        :is-reply="true"
        :current-user-id="currentUserId"
        @like="$emit('like', $event)"
        @delete="$emit('delete', $event)"
        @edit="onEdit"
        @reply="onReply"
        @report="$emit('report', $event)"
        @view-user="$emit('view-user', $event)"
      />
      <div
        v-if="review.replies.length > 3 && !showAllReplies"
        class="show-all-replies"
        @click="showAllReplies = true"
      >
        查看全部 {{ review.replies.length }} 条回复
      </div>
    </div>

    <div
      v-if="!isReply && review.replies && review.replies.length"
      class="conversation-entries"
    >
      <div
        v-for="reply in review.replies"
        :key="'conv-' + reply.id"
      >
        <div
          v-if="getThreadTotal(reply) >= 6"
          class="view-conversation"
          @click="$emit('view-conversation', review, reply)"
        >
          查看完整对话（{{ getThreadTotal(reply) }}条）&rarr;
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  review: { type: Object, required: true },
  isReply: { type: Boolean, default: false },
  currentUserId: { type: Number, default: null }
})

const emit = defineEmits(['like', 'delete', 'edit', 'reply', 'report', 'view-user', 'view-conversation'])

const showReplyInput = ref(false)
const replyContent = ref('')
const editing = ref(false)
const editContent = ref('')
const showAllReplies = ref(false)

const conversationModals = ref({})

function countDescendants(reply) {
  if (!reply.replies || reply.replies.length === 0) return 0
  let count = reply.replies.length
  for (const child of reply.replies) {
    count += countDescendants(child)
  }
  return count
}

function getThreadTotal(reply) {
  // 根评论(1) + 该二层回复(1) + 所有子孙
  return 2 + countDescendants(reply)
}

const sortedReplies = computed(() =>
  [...props.review.replies].sort((a, b) => b.likeCount - a.likeCount)
)

const displayReplies = computed(() =>
  showAllReplies.value ? sortedReplies.value : sortedReplies.value.slice(0, 3)
)

const isOwn = computed(() =>
  props.currentUserId && props.review.userId === props.currentUserId
)

const canEdit = computed(() => {
  if (!props.review.createdAt) return false
  const created = new Date(props.review.createdAt)
  const now = new Date()
  return (now - created) < 3 * 60 * 1000
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

function startEdit() {
  editContent.value = props.review.content
  editing.value = true
}

function doEdit() {
  if (!editContent.value.trim()) return
  emit('edit', props.review.id, editContent.value)
  editing.value = false
}

function doReply() {
  if (!replyContent.value.trim()) return
  emit('reply', props.review.id, replyContent.value)
  replyContent.value = ''
  showReplyInput.value = false
}

function onReply(id, content) {
  emit('reply', id, content)
}

function onEdit(id, content) {
  emit('edit', id, content)
}
</script>

<style scoped>
.review-item { padding: 16px 0; border-bottom: 1px solid var(--color-accent-light, #f0ebe0); }
.review-item.is-reply {
  margin-top: 8px;
  margin-left: 8px;
  padding: 12px;
  background: var(--color-bg, #fafaf7);
  border-radius: var(--radius, 8px);
  border-bottom: none;
}
.review-username {
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text, #4a3d2f);
  cursor: pointer;
  transition: color 0.2s;
}
.review-username:hover { color: var(--color-primary, #c9a96e); }
.review-me-tag {
  font-size: 11px;
  color: var(--color-text-muted, #a09880);
  font-weight: 400;
  margin-left: 4px;
}
.review-content {
  font-size: 13px;
  color: var(--color-text, #4a3d2f);
  line-height: 1.8;
  margin: 6px 0;
}
.review-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: var(--color-text-muted, #a09880);
  margin-top: 8px;
}
.meta-action {
  color: var(--color-text-secondary, #8b8070);
  cursor: pointer;
  transition: color 0.2s;
}
.meta-action:hover { color: var(--color-primary, #c9a96e); }
.meta-action.liked {
  color: var(--color-primary, #c9a96e);
  font-weight: 600;
}
.meta-danger { color: var(--color-text-secondary, #8b8070); }
.meta-danger:hover { color: var(--color-danger, #c04040); }
.review-edited-tag {
  font-size: 11px;
  color: var(--color-text-muted, #a09880);
  margin-top: 4px;
}
.review-edit-input {
  width: 100%;
  padding: 8px 10px;
  border: 1px solid #e0dbd0;
  border-radius: var(--radius-sm, 6px);
  font-size: 13px;
  resize: vertical;
  font-family: inherit;
  outline: none;
}
.review-edit-input:focus { border-color: var(--color-primary, #c9a96e); }
.review-edit-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
  margin-top: 8px;
}
.reply-input-area {
  margin-top: 10px;
  margin-left: 8px;
}
.reply-input {
  width: 100%;
  padding: 8px 10px;
  border: 1px solid #e0dbd0;
  border-radius: var(--radius-sm, 6px);
  font-size: 12px;
  resize: vertical;
  font-family: inherit;
  outline: none;
}
.reply-input:focus { border-color: var(--color-primary, #c9a96e); }
.reply-input-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
  margin-top: 6px;
}
.btn-cancel {
  padding: 4px 12px;
  background: var(--color-bg, #fafaf7);
  color: var(--color-text-secondary, #8b8070);
  border: 1px solid #e0dbd0;
  border-radius: var(--radius-sm, 6px);
  font-size: 12px;
  cursor: pointer;
}
.btn-save {
  padding: 4px 12px;
  background: var(--color-primary, #c9a96e);
  color: #fff;
  border: none;
  border-radius: var(--radius-sm, 6px);
  font-size: 12px;
  cursor: pointer;
}
.replies-list { margin-top: 4px; }
.show-all-replies {
  font-size: 12px;
  color: var(--color-primary, #c9a96e);
  cursor: pointer;
  padding: 6px 0 0 8px;
  transition: opacity 0.2s;
}
.show-all-replies:hover { opacity: 0.7; }
.reply-target-tag {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 6px;
  font-size: 12px;
}
.reply-target-label {
  color: var(--color-text-secondary, #8b8070);
}
.reply-target-username {
  color: var(--color-primary, #c9a96e);
  font-weight: 500;
  cursor: pointer;
}
.reply-target-username:hover {
  text-decoration: underline;
}
.reply-target-close {
  margin-left: auto;
  cursor: pointer;
  color: var(--color-text-muted, #a09880);
  font-size: 16px;
  line-height: 1;
}
.reply-target-close:hover {
  color: var(--color-text-secondary, #8b8070);
}
.conversation-entries {
  margin-top: 2px;
}
.view-conversation {
  font-size: 12px;
  color: var(--color-primary, #c9a96e);
  cursor: pointer;
  padding: 4px 0 2px 8px;
  transition: opacity 0.2s;
}
.view-conversation:hover {
  opacity: 0.7;
}
</style>
