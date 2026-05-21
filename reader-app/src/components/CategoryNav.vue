<template>
  <nav class="category-nav">
    <div class="category-header">
      <span class="category-title">图书分类</span>
      <button class="collapse-btn" @click="isCollapsed = !isCollapsed">
        {{ isCollapsed ? '▸' : '▾' }}
      </button>
    </div>

    <div class="category-list" v-show="!isCollapsed">
      <div v-for="cat in categories" :key="cat.id" class="category-group">
        <div
          class="category-item"
          :class="{ active: selectedId === cat.id }"
          @click="$emit('select', cat.id)"
        >
          <span class="cat-name">{{ cat.name }}</span>
          <span v-if="cat.children?.length" class="cat-arrow">
            {{ selectedId === cat.id ? '▾' : '▸' }}
          </span>
        </div>

        <div
          v-if="selectedId === cat.id && cat.children?.length"
          class="sub-list"
        >
          <div
            v-for="child in cat.children" :key="child.id"
            class="sub-item"
            :class="{ active: selectedChildId === child.id }"
            @click="$emit('select-child', child.id)"
          >{{ child.name }}</div>
        </div>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { ref } from 'vue'

defineProps({
  categories: { type: Array, default: () => [] },
  selectedId: { type: Number, default: null },
  selectedChildId: { type: Number, default: null }
})

defineEmits(['select', 'select-child'])

const isCollapsed = ref(false)
</script>

<style scoped>
.category-nav {
  width: var(--category-width, 180px);
  flex-shrink: 0;
}

.category-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 4px;
  margin-bottom: 8px;
  border-bottom: 2px solid var(--color-primary, #c9a96e);
}
.category-title {
  font-weight: 700;
  font-size: 16px;
  color: var(--color-text, #4a3d2f);
  font-family: var(--font-serif);
  letter-spacing: 1px;
}
.collapse-btn {
  background: none;
  border: none;
  font-size: 12px;
  color: var(--color-text-muted, #a09880);
  cursor: pointer;
  padding: 0 4px;
}

.category-list {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.category-group {
  display: flex;
  flex-direction: column;
}

/* 父级分类 */
.category-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 7px 12px;
  font-size: 15px;
  color: var(--color-text, #4a3d2f);
  cursor: pointer;
  border-radius: 6px;
  border-left: 3px solid transparent;
  transition: all 0.2s;
}
.category-item:hover {
  background: var(--color-accent-light, #f0ebe0);
  color: var(--color-primary, #c9a96e);
}
.category-item.active {
  background: var(--color-accent-light, #f0ebe0);
  color: var(--color-primary, #c9a96e);
  border-left-color: var(--color-primary, #c9a96e);
  font-weight: 600;
}
.cat-name { flex: 1; }
.cat-arrow {
  font-size: 10px;
  color: var(--color-text-muted, #a09880);
  transition: transform 0.2s;
}

/* 子分类 */
.sub-list {
  padding: 2px 0 4px 16px;
  display: flex;
  flex-direction: column;
  gap: 1px;
}
.sub-item {
  padding: 6px 12px;
  font-size: 14px;
  color: var(--color-text-secondary, #8b8070);
  cursor: pointer;
  border-radius: 6px;
  border-left: 2px solid transparent;
  transition: all 0.2s;
}
.sub-item:hover {
  background: var(--color-accent-light, #f0ebe0);
  color: var(--color-primary, #c9a96e);
}
.sub-item.active {
  background: var(--color-accent-light, #f0ebe0);
  color: var(--color-primary, #c9a96e);
  border-left-color: var(--color-primary, #c9a96e);
  font-weight: 500;
}

/* ===== 响应式 ===== */
@media (max-width: 768px) {
  .category-nav {
    width: 100%;
    border-bottom: 1px solid var(--color-border, #e8e4dc);
    padding-bottom: 10px;
    margin-bottom: 8px;
  }
  .category-header {
    border-bottom: none;
    margin-bottom: 4px;
  }
  .category-list {
    flex-direction: row;
    flex-wrap: wrap;
    gap: 6px;
  }
  .category-group { flex-direction: row; flex-wrap: wrap; }
  .category-item {
    padding: 5px 14px;
    font-size: 12px;
    background: var(--color-card-bg, #fff);
    border: 1px solid var(--color-border, #e8e4dc);
    border-radius: 16px;
    border-left: 1px solid var(--color-border, #e8e4dc);
  }
  .category-item.active {
    background: var(--color-primary, #c9a96e);
    color: #fff;
    border-color: var(--color-primary, #c9a96e);
  }
  .cat-arrow { display: none; }
  .sub-list {
    flex-direction: row;
    flex-wrap: wrap;
    padding: 0;
    gap: 4px;
    margin-left: 8px;
  }
  .sub-item {
    padding: 4px 10px;
    font-size: 11px;
    background: var(--color-accent-light, #f0ebe0);
    border-radius: 12px;
    border-left: none;
  }
  .sub-item.active {
    background: var(--color-primary, #c9a96e);
    color: #fff;
  }
}
</style>
