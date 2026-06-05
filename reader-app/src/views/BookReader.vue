<template>
  <div class="reader-page">
    <div class="reader-toolbar">
      <span class="toolbar-back" @click="$router.push(`/book/${bookId}`)">&larr; 返回详情</span>
      <select v-model="currentChapter" @change="onChapterChange" class="toolbar-chapter">
        <option v-for="(ch, i) in chapters" :key="i" :value="i">{{ ch.label }}</option>
      </select>
      <div class="toolbar-font">
        <span @click="fontSize = Math.max(12, fontSize - 2)">A-</span>
        <span @click="fontSize = Math.min(24, fontSize + 2)">A+</span>
      </div>
    </div>

    <div class="reader-viewer" ref="viewerRef"></div>

    <div class="reader-footer">
      <span @click="prevPage">上一页</span>
      <span v-if="totalPages > 0">{{ currentPage }} / {{ totalPages }}</span>
      <span @click="nextPage">下一页</span>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRoute } from 'vue-router'
import ePub from 'epubjs'

const route = useRoute()
const bookId = route.params.id

const viewerRef = ref(null)
const chapters = ref([])
const currentChapter = ref(0)
const currentPage = ref(1)
const totalPages = ref(0)
const fontSize = ref(16)

let book = null
let rendition = null

onMounted(async () => {
  try {
    const res = await fetch(`/api/books/${bookId}/epub`)
    if (!res.ok) throw new Error('EPUB加载失败')
    const buf = await res.arrayBuffer()
    book = ePub(buf)
    rendition = book.renderTo(viewerRef.value, {
      width: '100%',
      height: 'calc(100vh - 140px)',
      flow: 'paginated'
    })

    book.loaded.navigation.then(nav => {
      chapters.value = nav.toc.map(item => ({
        label: item.label,
        href: item.href
      }))
    })

    rendition.display()

    rendition.on('relocated', (loc) => {
      currentPage.value = loc.current + 1
      totalPages.value = loc.total
    })
  } catch (e) {
    console.error(e)
  }
})

onBeforeUnmount(() => {
  if (rendition) rendition.destroy()
  if (book) book.destroy()
})

watch(fontSize, (size) => {
  if (rendition) {
    rendition.themes.fontSize(`${size}px`)
  }
})

function prevPage() { if (rendition) rendition.prev() }
function nextPage() { if (rendition) rendition.next() }

function onChapterChange() {
  const ch = chapters.value[currentChapter.value]
  if (ch && rendition) {
    rendition.display(ch.href)
  }
}
</script>

<style scoped>
.reader-page {
  min-height: 100vh;
  background: var(--color-bg, #fafaf7);
  display: flex;
  flex-direction: column;
}
.reader-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 24px;
  background: #fff;
  border-bottom: 1px solid var(--color-border, #e8e4dc);
  position: sticky;
  top: 0;
  z-index: 10;
}
.toolbar-back {
  color: var(--color-primary, #c9a96e);
  cursor: pointer;
  font-size: 14px;
}
.toolbar-chapter {
  padding: 4px 12px;
  border: 1px solid #e0dbd0;
  border-radius: 6px;
  font-size: 13px;
  color: var(--color-text, #4a3d2f);
  max-width: 300px;
}
.toolbar-font {
  display: flex;
  gap: 12px;
  font-size: 13px;
  color: var(--color-text-secondary, #8b8070);
}
.toolbar-font span {
  cursor: pointer;
  padding: 2px 8px;
  border: 1px solid #e0dbd0;
  border-radius: 4px;
}
.toolbar-font span:hover { border-color: #c9a96e; }
.reader-viewer {
  flex: 1;
  padding: 16px 24px;
}
.reader-footer {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 24px;
  padding: 10px 24px;
  background: #fff;
  border-top: 1px solid var(--color-border, #e8e4dc);
  font-size: 13px;
  color: var(--color-text-secondary, #8b8070);
  position: sticky;
  bottom: 0;
}
.reader-footer span:first-child,
.reader-footer span:last-child {
  cursor: pointer;
  color: var(--color-primary, #c9a96e);
}
</style>