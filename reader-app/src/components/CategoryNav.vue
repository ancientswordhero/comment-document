<template>
  <nav class="category-nav">
    <div class="category-title">图书分类</div>
    <div
      v-for="cat in categories" :key="cat.id"
      class="category-item"
      :class="{ active: selectedId === cat.id }"
      @click="$emit('select', cat.id)"
    >{{ cat.name }}</div>
    <template v-for="cat in categories" :key="'sub-' + cat.id">
      <div
        v-if="selectedId === cat.id && cat.children?.length"
        v-for="child in cat.children" :key="child.id"
        class="category-item sub"
        :class="{ active: selectedChildId === child.id }"
        @click="$emit('select-child', child.id)"
      >{{ child.name }}</div>
    </template>
  </nav>
</template>

<script setup>
defineProps({
  categories: { type: Array, default: () => [] },
  selectedId: { type: Number, default: null },
  selectedChildId: { type: Number, default: null }
})
defineEmits(['select', 'select-child'])
</script>

<style scoped>
.category-nav { width: 160px; flex-shrink: 0; }
.category-title {
  font-weight: 600; font-size: 14px; color: var(--text);
  font-family: var(--font-serif); letter-spacing: 1px;
  margin-bottom: 10px;
}
.category-item {
  padding: 2px 10px; font-size: 13px; color: var(--text);
  cursor: pointer; line-height: 2.4; transition: color 0.2s;
}
.category-item:hover { color: var(--accent); }
.category-item.active {
  background: var(--accent-light);
  border-left: 2px solid var(--accent);
  font-weight: 500;
}
.category-item.sub {
  padding-left: 20px; font-size: 12px; color: var(--text-muted);
}
</style>
