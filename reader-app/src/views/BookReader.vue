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

/**
 * 移除 EPUB 内容中的 res:// 协议字体引用（Sony Reader 格式）
 * 浏览器环境下这些协议无法加载，会导致 CORS 错误
 */
function removeResProtocolFonts(doc) {
  if (!doc) return

  // 移除 <style> 中包含 res:// 的 @font-face 规则
  const styleElements = doc.querySelectorAll('style')
  styleElements.forEach(style => {
    if (style.textContent.includes('res://')) {
      // 移除所有引用 res:// 的 @font-face 规则
      style.textContent = style.textContent.replace(
        /@font-face\s*\{[^}]*res:\/\/[^}]*\}/gi,
        ''
      )
    }
  })

  // 移除 <link> 中引用 res:// 的字体链接
  const linkElements = doc.querySelectorAll('link[href]')
  linkElements.forEach(link => {
    if (link.getAttribute('href').startsWith('res://')) {
      link.remove()
    }
  })
}

onMounted(async () => {
  try {
    const res = await fetch(`/api/books/${bookId}/epub`)
    if (!res.ok) throw new Error('EPUB加载失败')
    const buf = await res.arrayBuffer()
    book = ePub(buf)

    // 注册内容钩子，移除 res:// 协议字体引用
    book.spine.hooks.content.register(removeResProtocolFonts)

    rendition = book.renderTo(viewerRef.value, {
      width: '100%',
      height: 'calc(100vh - 140px)',
      flow: 'paginated',
      allowScriptedContent: true
    })

    // 使用 book.ready 确保所有组件（spine/navigation/metadata等）加载完毕
    book.ready.then(() => {
      const nav = book.navigation
      if (nav && nav.toc && nav.toc.length > 0) {
        chapters.value = nav.toc.map(item => ({
          label: item.label,
          href: item.href
        }))
        // 尝试显示第一个章节，失败则回退到默认显示
        rendition.display(nav.toc[0].href).catch(() => {
          console.warn('无法通过 TOC href 定位章节，回退到默认起始位置')
          return rendition.display()
        })
      } else {
        rendition.display()
      }
    }).catch(err => {
      console.error('书籍加载失败:', err)
      // 即使 ready 失败，也尝试显示
      rendition.display()
    })

    rendition.on('relocated', (loc) => {
      currentPage.value = loc.current + 1
      totalPages.value = loc.total
    })
  } catch (e) {
    console.error('EPUB 加载失败:', e)
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
    rendition.display(ch.href).catch(() => {
      console.warn('章节跳转失败，尝试回退到默认位置')
      return rendition.display()
    })
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