<template>
  <Transition name="fade">
    <div v-if="visible" class="dialog-overlay" @click.self="$emit('close')">
      <div class="dialog-card" v-if="profile">
        <div class="profile-header">
          <div class="profile-avatar">{{ profile.username.charAt(0) }}</div>
          <div class="profile-header-info">
            <span v-if="!editing" class="profile-name">{{ profile.username }}</span>
            <input
              v-else
              v-model="editName"
              class="profile-name-input"
              maxlength="50"
              @keyup.enter="saveProfile"
            />
            <span class="profile-id">ID: {{ profile.id }}</span>
            <span class="profile-likes">获赞 {{ profile.totalLikes }}</span>
          </div>
        </div>
        <div class="profile-section">
          <div class="profile-section-title">个人留言</div>
          <p v-if="!editing" class="profile-bio">{{ profile.bio || '这个人很懒，什么都没写...' }}</p>
          <textarea
            v-else
            v-model="editBio"
            class="profile-bio-input"
            maxlength="200"
            rows="3"
            placeholder="写一句个人留言..."
          ></textarea>
        </div>
        <div class="profile-footer">
          <span class="profile-date">加入于 {{ formatSimpleDate(profile.createdAt) }}</span>
          <template v-if="isOwn">
            <button v-if="!editing" class="btn-edit" @click="startEdit">编辑资料</button>
            <template v-else>
              <button class="btn-cancel" @click="editing = false">取消</button>
              <button class="btn-save" :disabled="saving" @click="saveProfile">{{ saving ? '保存中...' : '保存' }}</button>
            </template>
          </template>
        </div>
      </div>
      <div v-else class="dialog-card loading">加载中...</div>
    </div>
  </Transition>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { getUserProfile, updateProfile } from '../api/user'
import { getUserIdFromToken } from '../utils/jwt'
import { formatSimpleDate } from '../utils/date.js'

const props = defineProps({ userId: Number, visible: Boolean })
const emit = defineEmits(['close'])

const profile = ref(null)
const editing = ref(false)
const editName = ref('')
const editBio = ref('')
const saving = ref(false)

const currentUserId = computed(() => {
  const token = localStorage.getItem('token')
  if (!token) return null
  try { return getUserIdFromToken(token) } catch { return null }
})

const isOwn = computed(() => currentUserId.value === props.userId)

watch(() => props.visible, async (v) => {
  if (v && props.userId) {
    editing.value = false
    profile.value = null
    try { profile.value = await getUserProfile(props.userId) } catch { profile.value = null }
  }
}, { immediate: true })

function startEdit() {
  editName.value = profile.value.username
  editBio.value = profile.value.bio || ''
  editing.value = true
}

async function saveProfile() {
  if (!editName.value.trim()) return
  saving.value = true
  try {
    profile.value = await updateProfile({ username: editName.value.trim(), bio: editBio.value.trim() || null })
    editing.value = false
  } finally { saving.value = false }
}
</script>

<style scoped>
.dialog-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.35); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.dialog-card { background: var(--color-card-bg, #fff); border-radius: var(--radius, 8px); padding: 32px; width: 380px; max-width: 90vw; box-shadow: var(--shadow-lg); }
.dialog-card.loading { text-align: center; color: var(--color-text-muted, #a09880); font-size: 13px; }
.profile-header { display: flex; gap: 16px; align-items: center; margin-bottom: 20px; }
.profile-avatar {
  width: 56px; height: 56px; border-radius: 50%;
  background: var(--color-primary, #c9a96e); color: #fff;
  display: flex; align-items: center; justify-content: center;
  font-size: 22px; font-family: var(--font-serif); flex-shrink: 0;
}
.profile-header-info { display: flex; flex-direction: column; gap: 4px; }
.profile-name { font-size: 18px; font-family: var(--font-serif); color: var(--color-text, #4a3d2f); font-weight: 600; }
.profile-name-input {
  font-size: 16px; padding: 4px 8px; border: 1px solid #e0dbd0; border-radius: var(--radius-sm, 6px);
  outline: none; font-family: inherit;
}
.profile-name-input:focus { border-color: var(--color-primary, #c9a96e); }
.profile-id { font-size: 12px; color: var(--color-text-muted, #a09880); }
.profile-likes { font-size: 12px; color: var(--color-primary, #c9a96e); }
.profile-section { margin-bottom: 16px; }
.profile-section-title { font-size: 12px; color: var(--color-text-muted, #a09880); margin-bottom: 6px; }
.profile-bio { font-size: 13px; color: var(--color-text-secondary, #8b8070); line-height: 1.6; margin: 0; }
.profile-bio-input {
  width: 100%; padding: 8px 10px; border: 1px solid #e0dbd0;
  border-radius: var(--radius-sm, 6px); font-size: 13px; resize: vertical;
  font-family: inherit; outline: none;
}
.profile-bio-input:focus { border-color: var(--color-primary, #c9a96e); }
.profile-footer { display: flex; justify-content: space-between; align-items: center; }
.profile-date { font-size: 11px; color: var(--color-text-muted, #a09880); }
.btn-edit { padding: 5px 14px; background: var(--color-bg, #fafaf7); color: var(--color-text-secondary, #8b8070); border: 1px solid #e0dbd0; border-radius: var(--radius-sm, 6px); font-size: 12px; cursor: pointer; }
.btn-cancel { padding: 5px 14px; background: var(--color-bg, #fafaf7); color: var(--color-text-secondary, #8b8070); border: 1px solid #e0dbd0; border-radius: var(--radius-sm, 6px); font-size: 12px; cursor: pointer; }
.btn-save { padding: 5px 14px; background: var(--color-primary, #c9a96e); color: #fff; border: none; border-radius: var(--radius-sm, 6px); font-size: 12px; cursor: pointer; }
.btn-save:disabled { opacity: 0.5; cursor: not-allowed; }
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
