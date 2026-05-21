<template>
  <Transition name="fade">
    <div v-if="visible" class="dialog-overlay" @click.self="$emit('close')">
      <div class="dialog-card">
        <h3 class="dialog-title">举报书评</h3>
        <div class="dialog-body">
          <div class="reason-list">
            <label
              v-for="opt in reasonOptions"
              :key="opt.value"
              class="reason-item"
              :class="{ selected: selectedReason === opt.value }"
            >
              <input type="radio" :value="opt.value" v-model="selectedReason" class="reason-radio" />
              <span class="reason-label">{{ opt.label }}</span>
            </label>
          </div>
          <div v-if="selectedReason === 'other'" class="detail-area">
            <textarea v-model="detail" class="detail-input" placeholder="请描述具体理由（必填，200字以内）" maxlength="200" rows="3"></textarea>
          </div>
        </div>
        <div class="dialog-actions">
          <button class="btn-cancel" @click="$emit('close')">取消</button>
          <button class="btn-submit" :disabled="!canSubmit || submitting" @click="doSubmit">{{ submitting ? '提交中...' : '提交举报' }}</button>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { ref, computed } from 'vue'
import { reportReview } from '../api/report'

const props = defineProps({ reviewId: { type: Number, required: true }, visible: Boolean })
const emit = defineEmits(['close', 'done'])

const selectedReason = ref('')
const detail = ref('')
const submitting = ref(false)

const reasonOptions = [
  { value: 'spam', label: '垃圾广告' },
  { value: 'abuse', label: '人身攻击' },
  { value: 'fake', label: '虚假信息' },
  { value: 'violation', label: '违规内容' },
  { value: 'other', label: '其他' }
]

const canSubmit = computed(() => {
  if (!selectedReason.value) return false
  if (selectedReason.value === 'other' && !detail.value.trim()) return false
  return true
})

async function doSubmit() {
  if (!canSubmit.value) return
  submitting.value = true
  try {
    await reportReview(props.reviewId, { reason: selectedReason.value, detail: selectedReason.value === 'other' ? detail.value.trim() : undefined })
    emit('done')
  } finally { submitting.value = false }
}
</script>

<style scoped>
.dialog-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.35); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.dialog-card { background: var(--color-card-bg, #fff); border-radius: var(--radius, 8px); padding: 24px; width: 400px; max-width: 90vw; box-shadow: var(--shadow-lg); }
.dialog-title { font-size: 16px; font-family: var(--font-serif); color: var(--color-text, #4a3d2f); margin-bottom: 16px; font-weight: 600; }
.reason-list { display: flex; flex-direction: column; gap: 2px; }
.reason-item { display: flex; align-items: center; gap: 8px; padding: 10px 12px; border-radius: var(--radius-sm, 6px); cursor: pointer; transition: background 0.2s; }
.reason-item:hover { background: var(--color-accent-light, #f0ebe0); }
.reason-item.selected { background: var(--color-accent-light, #f0ebe0); }
.reason-radio { accent-color: var(--color-primary, #c9a96e); }
.reason-label { font-size: 13px; color: var(--color-text, #4a3d2f); }
.detail-area { margin-top: 12px; }
.detail-input { width: 100%; padding: 8px 10px; border: 1px solid #e0dbd0; border-radius: var(--radius-sm, 6px); font-size: 12px; resize: vertical; font-family: inherit; outline: none; }
.detail-input:focus { border-color: var(--color-primary, #c9a96e); }
.dialog-actions { display: flex; gap: 10px; justify-content: flex-end; margin-top: 20px; }
.btn-cancel { padding: 6px 16px; background: var(--color-bg, #fafaf7); color: var(--color-text-secondary, #8b8070); border: 1px solid #e0dbd0; border-radius: var(--radius-sm, 6px); font-size: 13px; cursor: pointer; }
.btn-submit { padding: 6px 16px; background: var(--color-primary, #c9a96e); color: #fff; border: none; border-radius: var(--radius-sm, 6px); font-size: 13px; cursor: pointer; }
.btn-submit:disabled { opacity: 0.5; cursor: not-allowed; }
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
