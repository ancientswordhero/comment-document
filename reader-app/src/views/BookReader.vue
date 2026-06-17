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

    <!-- 选区操作浮泡 -->
    <div
      v-if="showBubble"
      class="selection-bubble"
      :style="bubbleStyle"
    >
      <button @click="onCopy">复制</button>
      <button @click="onHighlight">划线</button>
      <button @click="onAnnotate">记书余</button>
    </div>

    <!-- 撰写浮层 -->
    <div v-if="showAnnotate" class="annotate-panel">
      <div class="annotate-topbar">
        <span class="annotate-topbar-title">记书余</span>
        <button class="annotate-close" @click="onCancelAnnotate">&times;</button>
      </div>
      <div class="annotate-header">
        <button
          :class="['annotate-tab', { active: annotateType === 'QUESTION' }]"
          @click="annotateType = 'QUESTION'"
        >疑问</button>
        <button
          :class="['annotate-tab', { active: annotateType === 'INSIGHT' }]"
          @click="annotateType = 'INSIGHT'"
        >心得</button>
      </div>
      <blockquote class="annotate-quote">"{{ selectedText }}"</blockquote>
      <textarea
        v-model="annotateContent"
        class="annotate-textarea"
        :placeholder="annotateType === 'QUESTION' ? '你哪里困惑？' : '写下你的心得...'"
      ></textarea>
      <div class="annotate-footer">
        <label class="annotate-toggle">
          <input type="checkbox" v-model="syncToYuyin" />
          <span>同步至余音</span>
        </label>
        <div class="annotate-footer-btns">
          <button class="annotate-cancel" @click="onCancelAnnotate">取消</button>
          <button class="annotate-submit" @click="onSaveNote">留墨</button>
        </div>
      </div>
    </div>

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
import { createNote } from '../api/note'

const route = useRoute()
const bookId = route.params.id

const viewerRef = ref(null)
const chapters = ref([])
const currentChapter = ref(0)
const currentPage = ref(1)
const totalPages = ref(0)
const fontSize = ref(16)

// 批注相关状态
const showBubble = ref(false)
const bubbleStyle = ref({})
const selectedText = ref('')
const selectedCfi = ref('')
const showAnnotate = ref(false)
const annotateType = ref('INSIGHT')
const annotateContent = ref('')
const syncToYuyin = ref(false)

let lastSelection = null

let book = null
let rendition = null
let sandboxObserver = null

/**
 * 修复 epubjs iframe 的 sandbox 属性
 * epubjs 默认给 iframe 加了 sandbox="allow-scripts allow-same-origin"，
 * 这会阻止选区和点击事件跨 iframe 传递，导致 selected 事件无法触发。
 */
function fixIframeSandbox(container) {
  const iframes = container.querySelectorAll('iframe')
  iframes.forEach(iframe => {
    // 完全移除 sandbox，让 iframe 与主文档正常通信
    iframe.removeAttribute('sandbox')
  })
}

/**
 * 监听 viewer 容器中新增的 iframe，自动移除 sandbox
 * epubjs 在翻页/换章时会动态创建新 iframe
 */
function startSandboxObserver() {
  const container = viewerRef.value
  if (!container) return

  // 立即处理已有的 iframe
  fixIframeSandbox(container)

  // 监听后续新增的 iframe
  sandboxObserver = new MutationObserver((mutations) => {
    for (const mutation of mutations) {
      for (const node of mutation.addedNodes) {
        if (node.nodeName === 'IFRAME') {
          node.removeAttribute('sandbox')
        } else if (node.querySelectorAll) {
          fixIframeSandbox(node)
        }
      }
    }
  })
  sandboxObserver.observe(container, { childList: true, subtree: true })
}

/**
 * 移除 EPUB 内容中的 res:// 协议字体引用（Sony Reader 格式）
 * 浏览器环境下这些协议无法加载，会导致 CORS 错误
 */
function removeResProtocolFonts(doc) {
  if (!doc) return

  const styleElements = doc.querySelectorAll('style')
  styleElements.forEach(style => {
    if (style.textContent.includes('res://')) {
      style.textContent = style.textContent.replace(
        /@font-face\s*\{[^}]*res:\/\/[^}]*\}/gi,
        ''
      )
    }
  })

  const linkElements = doc.querySelectorAll('link[href]')
  linkElements.forEach(link => {
    if (link.getAttribute('href').startsWith('res://')) {
      link.remove()
    }
  })
}

/**
 * 注入划线样式到 EPUB 内容的 iframe 中
 */
function injectHighlightStyles(doc) {
  if (!doc) return
  const style = doc.createElement('style')
  style.textContent = `
    .epubjs-hl, [class*="epubjs-hl"] {
      background: rgba(201,169,110,0.4) !important;
      border-radius: 2px;
      cursor: pointer;
      transition: background 0.2s;
    }
    .epubjs-hl:hover, [class*="epubjs-hl"]:hover {
      background: rgba(201,169,110,0.65) !important;
    }
  `
  doc.head.appendChild(style)
}

onMounted(async () => {
  try {
    const res = await fetch(`/api/books/${bookId}/epub`)
    if (!res.ok) throw new Error('EPUB加载失败')
    const buf = await res.arrayBuffer()
    book = ePub(buf)

    // 注册内容钩子
    book.spine.hooks.content.register(removeResProtocolFonts)
    book.spine.hooks.content.register(injectHighlightStyles)

    rendition = book.renderTo(viewerRef.value, {
      width: '100%',
      height: 'calc(100vh - 140px)',
      flow: 'paginated',
      allowScriptedContent: true
    })

    // 启动 sandbox 修复：解决 iframe 阻止选区事件的问题
    startSandboxObserver()

    // 使用 book.ready 确保所有组件加载完毕
    book.ready.then(() => {
      const nav = book.navigation
      if (nav && nav.toc && nav.toc.length > 0) {
        chapters.value = nav.toc.map(item => ({
          label: item.label,
          href: item.href
        }))
        rendition.display(nav.toc[0].href).catch(() => {
          console.warn('无法通过 TOC href 定位章节，回退到默认起始位置')
          return rendition.display()
        })
      } else {
        rendition.display()
      }
    }).catch(err => {
      console.error('书籍加载失败:', err)
      rendition.display()
    })

    // 听文本选区事件
    rendition.on('selected', (cfiRange, contents) => {
      // epubjs 的 contents 可能是 { document, window } 或直接是 document
      const win = contents.window || contents.defaultView
      const doc = contents.document || contents
      const selection = win ? win.getSelection() : (doc.getSelection ? doc.getSelection() : null)
      if (!selection) return

      const text = selection.toString().trim()
      if (!text || text.length < 1) {
        showBubble.value = false
        return
      }
      selectedText.value = text
      selectedCfi.value = cfiRange
      lastSelection = selection

      const range = selection.getRangeAt(0)
      const rect = range.getBoundingClientRect()
      bubbleStyle.value = {
        left: `${rect.left + rect.width / 2 - 60}px`,
        top: `${rect.top - 44}px`
      }
      showBubble.value = true
    })

    // 点击阅读器空白处关闭浮泡
    rendition.on('click', () => {
      showBubble.value = false
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
  if (sandboxObserver) sandboxObserver.disconnect()
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

function onCopy() {
  navigator.clipboard.writeText(selectedText.value).catch(() => {})
  showBubble.value = false
}

function onHighlight() {
  if (rendition && lastSelection) {
    rendition.annotations.highlight(
      lastSelection.getRangeAt(0),
      {},
      (e) => {
        // 回调：应用自定义样式使划线更明显
        e.addEventListener('click', () => {
          // 点击已有划线可移除
          if (confirm('要移除此划线吗？')) {
            rendition.annotations.remove(e, 'highlight')
          }
        })
      },
      'highlight',
      { fill: 'rgba(201,169,110,0.35)', 'fill-opacity': '0.35' }
    )
  }
  showBubble.value = false
}

function onCancelAnnotate() {
  showAnnotate.value = false
  annotateContent.value = ''
}

function onAnnotate() {
  showBubble.value = false
  showAnnotate.value = true
  annotateContent.value = ''
  annotateType.value = 'INSIGHT'
  syncToYuyin.value = false
}

async function onSaveNote() {
  if (!annotateContent.value.trim()) return
  try {
    await createNote(bookId, {
      content: annotateContent.value.trim(),
      selectedText: selectedText.value,
      cfi: selectedCfi.value,
      type: annotateType.value,
      publish: syncToYuyin.value
    })
    showAnnotate.value = false
    if (rendition && lastSelection) {
      rendition.annotations.highlight(
        lastSelection.getRangeAt(0),
        {},
        null,
        'annotation'
      )
    }
    alert('留墨成功')
  } catch (e) {
    const msg = e?.response?.data?.message || '保存失败'
    alert(msg)
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

/* 选区操作浮泡 */
.selection-bubble {
  position: fixed;
  z-index: 50;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.12);
  display: flex;
  padding: 4px;
  gap: 2px;
}
.selection-bubble button {
  padding: 4px 12px;
  border: none;
  background: none;
  font-size: 12px;
  color: var(--color-text, #4a3d2f);
  cursor: pointer;
  border-radius: 6px;
  transition: background 0.15s;
}
.selection-bubble button:hover { background: #f5f0e5; }

/* 撰写浮层 */
.annotate-panel {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 60;
  background: #fff;
  border-radius: 16px 16px 0 0;
  box-shadow: 0 -2px 16px rgba(0,0,0,0.1);
  padding: 0 24px 24px;
  max-height: 60vh;
  overflow-y: auto;
}
.annotate-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 0 12px;
  border-bottom: 1px solid #f0ebe0;
  margin-bottom: 14px;
  position: sticky;
  top: 0;
  background: #fff;
  z-index: 1;
}
.annotate-topbar-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text, #4a3d2f);
}
.annotate-close {
  width: 28px;
  height: 28px;
  border: none;
  background: #f5f5f5;
  border-radius: 50%;
  font-size: 18px;
  color: var(--color-text-secondary, #8b8070);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
  transition: all 0.15s;
}
.annotate-close:hover { background: #e8e4dc; color: #4a3d2f; }
.annotate-header {
  display: flex;
  gap: 16px;
  margin-bottom: 14px;
}
.annotate-tab {
  padding: 4px 16px;
  border: 1px solid #e0dbd0;
  border-radius: 14px;
  background: none;
  font-size: 13px;
  color: var(--color-text-secondary, #8b8070);
  cursor: pointer;
  transition: all 0.2s;
}
.annotate-tab.active {
  background: var(--color-primary, #c9a96e);
  color: #fff;
  border-color: transparent;
}
.annotate-quote {
  font-size: 13px;
  color: var(--color-text-muted, #a09880);
  border-left: 2px solid #e0dbd0;
  padding-left: 10px;
  margin-bottom: 12px;
  font-style: italic;
}
.annotate-textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #e0dbd0;
  border-radius: 8px;
  font-size: 14px;
  min-height: 80px;
  resize: vertical;
  font-family: var(--font-sans);
  color: var(--color-text, #4a3d2f);
}
.annotate-textarea:focus { outline: none; border-color: var(--color-primary, #c9a96e); }
.annotate-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 14px;
}
.annotate-toggle {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--color-text-secondary, #8b8070);
  cursor: pointer;
}
.annotate-footer-btns {
  display: flex;
  gap: 10px;
}
.annotate-cancel {
  padding: 8px 20px;
  background: #f5f5f5;
  border: 1px solid #e0dbd0;
  border-radius: 8px;
  font-size: 14px;
  color: var(--color-text-secondary, #8b8070);
  cursor: pointer;
}
.annotate-cancel:hover { background: #e8e4dc; }
.annotate-submit {
  padding: 8px 28px;
  background: var(--color-primary, #c9a96e);
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  font-family: var(--font-serif);
  letter-spacing: 2px;
}
.annotate-submit:hover { background: var(--color-primary-hover, #b8944d); }
</style>
