# 评论回复提示 & 完整对话视图 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在评论区添加回复对象标签，并在子对话超过5条消息时提供弹窗查看完整对话时间线。

**Architecture:** 后端修改 ReviewService 将回复分组方式从按 root_id 改为按 parent_id，使前端获得完整嵌套树；前端新增 ConversationModal 弹窗组件，ReviewItem 添加回复标签和对话入口。

**Tech Stack:** Java 17 / Spring Boot 3 / JPA (后端), Vue 3 / Composition API (前端)

---

### Task 1: 后端 — 递归填充回复嵌套

**Files:**
- Modify: `library-server/src/main/java/com/library/service/ReviewService.java:50-53,187-217`

- [ ] **Step 1: 修改 getReviews() 中 repliesMap 的构建方式**

将 `repliesMap` 从「按 root_id 分组」改为「按 parent_id 分组」，使任意评论都能找到其直接子回复。

找到第 50-53 行：
```java
Map<Long, List<Review>> repliesMap = new HashMap<>();
for (Long id : reviewIds) {
    List<Review> replies = reviewRepository.findByRootIdOrderByCreatedAtAsc(id);
    repliesMap.put(id, replies);
}
```

改为：
```java
Map<Long, List<Review>> childrenMap = new HashMap<>();
for (Long id : reviewIds) {
    List<Review> allReplies = reviewRepository.findByRootIdOrderByCreatedAtAsc(id);
    for (Review r : allReplies) {
        childrenMap.computeIfAbsent(r.getParentId(), k -> new ArrayList<>()).add(r);
    }
}
```

- [ ] **Step 2: 修改 getReviews() 中 allReviewIds 的收集方式**

找到第 56-61 行：
```java
Set<Long> allReviewIds = new HashSet<>(reviewIds);
for (List<Review> replies : repliesMap.values()) {
    for (Review r : replies) {
        allReviewIds.add(r.getId());
    }
}
```

改为（变量名从 `repliesMap` 改为 `childrenMap`）：
```java
Set<Long> allReviewIds = new HashSet<>(reviewIds);
for (List<Review> children : childrenMap.values()) {
    for (Review r : children) {
        allReviewIds.add(r.getId());
    }
}
```

- [ ] **Step 3: 修改 getReviews() 中 toResponse 调用**

找到第 73-74 行：
```java
List<ReviewResponse> records = reviewPage.getContent().stream()
    .map(r -> toResponse(r, repliesMap, finalLikedIds, usernameCache))
    .collect(Collectors.toList());
```

改为（变量名保持一致）：
```java
List<ReviewResponse> records = reviewPage.getContent().stream()
    .map(r -> toResponse(r, childrenMap, finalLikedIds, usernameCache))
    .collect(Collectors.toList());
```

- [ ] **Step 4: 修改 toResponse() 递归填充所有层级**

找到第 194-200 行：
```java
List<ReviewResponse> replies = List.of();
if (review.getParentId() == null) {
    List<Review> childReplies = repliesMap.getOrDefault(review.getId(), List.of());
    replies = childReplies.stream()
        .map(r -> toResponse(r, Collections.emptyMap(), likedIds, usernameCache))
        .collect(Collectors.toList());
}
```

改为（移除 parentId == null 限制，递归使用同一个 map）：
```java
List<ReviewResponse> replies = childrenMap.getOrDefault(review.getId(), List.of())
    .stream()
    .map(r -> toResponse(r, childrenMap, likedIds, usernameCache))
    .collect(Collectors.toList());
```

- [ ] **Step 5: 运行现有测试确保不破坏已有功能**

```bash
cd library-server && mvn test -Dtest=ReviewServiceTest -q
```
预期：全部 10 个测试通过。

- [ ] **Step 6: Commit**

```bash
git add library-server/src/main/java/com/library/service/ReviewService.java
git commit -m "feat: recursively nest review replies by parent_id"
```

---

### Task 2: 后端 — 添加嵌套回复的测试

**Files:**
- Modify: `library-server/src/test/java/com/library/service/ReviewServiceTest.java`

- [ ] **Step 1: 在 ReviewServiceTest 中添加测试方法**

在类末尾（第 192 行 `}` 之前）添加：

```java
@Test
void shouldNestRepliesRecursively() {
    // Setup: root review with one direct reply that has its own child reply
    Review root = Review.builder().id(1L).bookId(10L).userId(1L)
        .content("根评论").likeCount(0).replyCount(2).build();
    Review reply1 = Review.builder().id(2L).bookId(10L).userId(2L)
        .parentId(1L).rootId(1L).content("回复1").likeCount(1).replyCount(0)
        .createdAt(LocalDateTime.now().minusHours(1)).build();
    Review reply2 = Review.builder().id(3L).bookId(10L).userId(3L)
        .parentId(2L).rootId(1L).content("回复1的回复").likeCount(0).replyCount(0)
        .createdAt(LocalDateTime.now()).build();

    Page<Review> page = new PageImpl<>(List.of(root));
    when(reviewRepository.findByBookIdAndParentIdIsNull(eq(10L), any(Pageable.class)))
        .thenReturn(page);
    when(reviewRepository.findByRootIdOrderByCreatedAtAsc(1L))
        .thenReturn(List.of(reply1, reply2));
    when(userRepository.findById(1L)).thenReturn(Optional.of(
        new User(1L, "楼主", "pw", "READER", null)));
    when(userRepository.findById(2L)).thenReturn(Optional.of(
        new User(2L, "回复者", "pw", "READER", null)));
    when(userRepository.findById(3L)).thenReturn(Optional.of(
        new User(3L, "子回复者", "pw", "READER", null)));

    PageResult<ReviewResponse> result = reviewService.getReviews(10L, "time", 1, 10, null);

    assertThat(result.getRecords()).hasSize(1);
    ReviewResponse rootResp = result.getRecords().get(0);
    // 根评论有1条直接回复
    assertThat(rootResp.getReplies()).hasSize(1);
    ReviewResponse directReply = rootResp.getReplies().get(0);
    assertThat(directReply.getContent()).isEqualTo("回复1");
    // 该回复有1条子回复（递归嵌套）
    assertThat(directReply.getReplies()).hasSize(1);
    assertThat(directReply.getReplies().get(0).getContent()).isEqualTo("回复1的回复");
}
```

- [ ] **Step 2: 运行测试验证嵌套逻辑**

```bash
cd library-server && mvn test -Dtest=ReviewServiceTest#shouldNestRepliesRecursively -q
```
预期：PASS

- [ ] **Step 3: 运行全部测试**

```bash
cd library-server && mvn test -Dtest=ReviewServiceTest -q
```
预期：全部 11 个测试通过。

- [ ] **Step 4: Commit**

```bash
git add library-server/src/test/java/com/library/service/ReviewServiceTest.java
git commit -m "test: add nested reply recursion test"
```

---

### Task 3: 前端 — ReviewItem 回复标签

**Files:**
- Modify: `reader-app/src/components/ReviewItem.vue`

- [ ] **Step 1: 在回复输入区添加标签**

将第 35-41 行的回复输入区模板：

```html
<div v-if="showReplyInput" class="reply-input-area">
  <textarea v-model="replyContent" class="reply-input" rows="2" placeholder="写下你的回复..."></textarea>
  <div class="reply-input-actions">
    <button class="btn-cancel" @click="showReplyInput = false; replyContent = ''">取消</button>
    <button class="btn-save" @click="doReply">回复</button>
  </div>
</div>
```

改为：

```html
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
```

- [ ] **Step 2: 添加标签样式**

在 `</style>` 前（第 265 行前）添加：

```css
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
```

- [ ] **Step 3: Commit**

```bash
git add reader-app/src/components/ReviewItem.vue
git commit -m "feat: add reply target tag showing @username in ReviewItem"
```

---

### Task 4: 前端 — ReviewItem 对话入口

**Files:**
- Modify: `reader-app/src/components/ReviewItem.vue`

- [ ] **Step 1: 添加对话消息计数函数和入口状态**

在 script setup 中（第 84 行 `const showAllReplies = ref(false)` 后）添加：

```js
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
```

- [ ] **Step 2: 添加「查看完整对话」入口模板**

在第 63 行 `查看全部 N 条回复` 链接的 `</div>` 之后（即 `</div>` 第 65 行后）、`</template>` 之前，添加：

```html
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
```

- [ ] **Step 3: 添加入口样式**

在 `</style>` 前添加：

```css
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
```

- [ ] **Step 4: 注册新事件**

在 emit 定义（第 78 行）中添加 `'view-conversation'`：

```js
const emit = defineEmits(['like', 'delete', 'edit', 'reply', 'report', 'view-user', 'view-conversation'])
```

- [ ] **Step 5: Commit**

```bash
git add reader-app/src/components/ReviewItem.vue
git commit -m "feat: add view conversation entry for threads with >=6 messages"
```

---

### Task 5: 前端 — ConversationModal 组件

**Files:**
- Create: `reader-app/src/components/ConversationModal.vue`

- [ ] **Step 1: 创建组件文件**

创建 `reader-app/src/components/ConversationModal.vue`：

```vue
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

const isLoggedIn = computed(() => !!localStorage.getItem('token'))

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

function getParentUsername(parentId) {
  const parent = flatMessages.value.find(m => m.id === parentId)
  return parent ? parent.username : '未知用户'
}

watch(() => props.visible, async (v) => {
  if (v) {
    replyingTo.value = null
    replyContent.value = ''
    newReplyContent.value = ''
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
  background: #fff;
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
  border-bottom: 1px solid #e8e4dc;
  flex-shrink: 0;
}
.conv-title {
  font-size: 16px;
  font-weight: 600;
  color: #4a3d2f;
}
.conv-close {
  font-size: 22px;
  color: #a09880;
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
  color: #4a3d2f;
  cursor: pointer;
}
.conv-msg-user:hover { color: #c9a96e; }
.conv-me-tag {
  font-size: 11px;
  color: #a09880;
  margin-left: 2px;
}
.conv-msg-time {
  color: #a09880;
  margin-left: 8px;
}
.conv-msg-content {
  font-size: 13px;
  color: #4a3d2f;
  line-height: 1.7;
  margin: 4px 0;
}
.conv-reply-prefix {
  color: #c9a96e;
  font-size: 12px;
}
.conv-msg-actions {
  display: flex;
  gap: 14px;
  font-size: 12px;
  color: #a09880;
  margin-top: 2px;
}
.conv-action {
  cursor: pointer;
  transition: color 0.2s;
}
.conv-action:hover { color: #c9a96e; }
.conv-action.liked {
  color: #c9a96e;
  font-weight: 600;
}
.conv-action-danger:hover { color: #c04040; }
.conv-edited-tag {
  font-size: 11px;
  color: #a09880;
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
.conv-edit-input:focus { border-color: #c9a96e; }
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
.conv-reply-input:focus { border-color: #c9a96e; }
.conv-reply-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
  margin-top: 6px;
}
.conv-footer {
  padding: 12px 20px;
  border-top: 1px solid #e8e4dc;
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
.conv-footer-input:focus { border-color: #c9a96e; }
.conv-footer-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 6px;
}
.conv-footer-login {
  padding: 12px 20px;
  border-top: 1px solid #e8e4dc;
  text-align: center;
  font-size: 13px;
  color: #a09880;
  flex-shrink: 0;
}
.conv-footer-login a {
  color: #c9a96e;
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
  color: #c9a96e;
  font-weight: 500;
  cursor: pointer;
}
.reply-target-username:hover { text-decoration: underline; }
.reply-target-close {
  margin-left: auto;
  cursor: pointer;
  color: #a09880;
  font-size: 16px;
  line-height: 1;
}
.reply-target-close:hover { color: #8b8070; }
.btn-cancel {
  padding: 4px 12px;
  background: #fafaf7;
  color: #8b8070;
  border: 1px solid #e0dbd0;
  border-radius: 6px;
  font-size: 12px;
  cursor: pointer;
}
.btn-save {
  padding: 4px 12px;
  background: #c9a96e;
  color: #fff;
  border: none;
  border-radius: 6px;
  font-size: 12px;
  cursor: pointer;
}
.btn-save:disabled { opacity: 0.5; cursor: not-allowed; }
</style>
```

- [ ] **Step 2: Commit**

```bash
git add reader-app/src/components/ConversationModal.vue
git commit -m "feat: add ConversationModal for full thread view"
```

---

### Task 6: 前端 — ReviewSection 集成弹窗

**Files:**
- Modify: `reader-app/src/components/ReviewSection.vue`

- [ ] **Step 1: 导入 ConversationModal 组件**

在第 84 行 import 区域添加：

```js
import ConversationModal from './ConversationModal.vue'
```

- [ ] **Step 2: 添加弹窗状态**

在第 108 行 `const showUserDialog = ref(false)` 后添加：

```js
const convVisible = ref(false)
const convRootReview = ref(null)
const convThreadReply = ref(null)
```

- [ ] **Step 3: 添加打开弹窗的处理函数**

在 `function onViewUser`（第 113 行）后添加：

```js
function onViewConversation(rootReview, threadReply) {
  convRootReview.value = rootReview
  convThreadReply.value = threadReply
  convVisible.value = true
}
```

- [ ] **Step 4: 添加弹窗事件处理函数**

在 `onViewConversation` 后添加：

```js
function onConvClose() {
  convVisible.value = false
  convRootReview.value = null
  convThreadReply.value = null
}

async function onConvReply(parentId, content) {
  await createReply(parentId, { content })
  await fetchReviews()
  // 更新弹窗数据：在更新后的 reviews 中找到对应的根评论和线程
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
  // 更新弹窗内的点赞状态
  toggleLikeLocal([convRootReview.value], reviewId)
}

async function onConvDelete(reviewId) {
  if (!confirm('确定删除这条评论吗？')) return
  await deleteReview(reviewId)
  await fetchReviews()
  // 检查线程是否仍然存在且消息数 >= 6
  const updatedRoot = reviews.value.find(r => r.id === convRootReview.value.id)
  if (updatedRoot) {
    convRootReview.value = updatedRoot
    const updatedThread = updatedRoot.replies.find(r => r.id === convThreadReply.value.id)
    if (updatedThread) {
      // 重新计算线程消息数
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
```

- [ ] **Step 5: 在模板中添加弹窗和事件绑定**

在 `ReviewItem` 组件（第 43 行）添加 `@view-conversation` 事件：

```html
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
```

在 `UserProfileDialog`（第 72-77 行）之后、`</template>` 之前添加：

```html
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
```

- [ ] **Step 6: Commit**

```bash
git add reader-app/src/components/ReviewSection.vue
git commit -m "feat: integrate ConversationModal into ReviewSection"
```

---

### Task 7: 前端 — 验证

**Files:** 无新建

- [ ] **Step 1: 启动 reader-app 开发服务器**

```bash
cd reader-app && npm run dev
```

- [ ] **Step 2: 手动验证以下场景**

1. **回复标签：** 点击任意评论的「回复」，确认输入区上方出现 `回复 @用户名` 标签，点击用户名跳转主页，点击 × 关闭
2. **对话入口：** 找一条有 ≥6 条线程消息的二层回复，确认出现「查看完整对话（N条）」链接
3. **弹窗打开：** 点击链接，确认弹窗按时间旧→新展示所有消息
4. **弹窗回复：** 在弹窗底部输入框发送回复，确认消息追加到列表末尾
5. **弹窗点赞/编辑/删除：** 确认各项操作正常
6. **删除后降级：** 删除一条消息后线程数降回 <6，确认弹窗关闭
7. **未登录：** 未登录状态下打开弹窗，确认底部显示登录提示

- [ ] **Step 3: 修复发现的问题后提交**

```bash
git add -A && git commit -m "chore: verify conversation feature end-to-end"
```

---

## 测试清单

| 场景 | 预期 |
|---|---|
| 线程消息 <6 条 | 不显示「查看完整对话」入口 |
| 线程消息 ≥6 条 | 显示入口，弹窗正常 |
| 弹窗中回复 | 消息追加到列表底部 |
| 弹窗中点赞 | 计数更新，图标高亮 |
| 弹窗中编辑（3分钟内） | 内容更新 |
| 弹窗中编辑（超时） | 不显示编辑按钮 |
| 弹窗中删除 | 消息消失，<6 条时弹窗关闭 |
| 点击「回复 @用户名」标签中用户名 | 弹出用户主页 |
| 弹窗内回复某人 | 显示「回复 @用户名：」前缀 |
| 未登录查看弹窗 | 底部显示登录提示 |
| 手机端弹窗 | 宽度适配 95vw |
