<template>
  <div class="pagination" v-if="totalPages > 1">
    <button :disabled="page <= 1" @click="$emit('change', page - 1)">← 上一页</button>
    <button
      v-for="p in displayedPages" :key="p"
      :class="{ active: p === page }"
      @click="$emit('change', p)"
    >{{ p }}</button>
    <button :disabled="page >= totalPages" @click="$emit('change', page + 1)">下一页 →</button>
  </div>
</template>

<script setup>
import { computed } from 'vue'
const props = defineProps({ page: Number, total: Number, size: Number })
defineEmits(['change'])
const totalPages = computed(() => Math.ceil(props.total / props.size))
const displayedPages = computed(() => {
  const pages = []; const total = totalPages.value; const current = props.page
  let start = Math.max(1, current - 2); let end = Math.min(total, current + 2)
  if (end - start < 4) {
    if (start === 1) end = Math.min(total, start + 4)
    else start = Math.max(1, end - 4)
  }
  for (let i = start; i <= end; i++) pages.push(i)
  return pages
})
</script>

<style scoped>
.pagination {
  text-align: center;
  margin-top: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}
.pagination button {
  padding: 6px 12px;
  border: 1px solid var(--color-border);
  background: var(--color-card-bg);
  border-radius: var(--radius);
  font-size: 12px;
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: all 0.2s;
}
.pagination button:hover:not(:disabled) {
  border-color: var(--color-primary);
  color: var(--color-primary);
}
.pagination button.active {
  background: var(--color-primary);
  color: #fff;
  border-color: var(--color-primary);
  font-weight: 500;
}
.pagination button:disabled {
  color: #d0c8b4;
  cursor: not-allowed;
}
</style>
