# EPUB 图书上传与阅读 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 支持上传 EPUB 格式图书并在读者端使用 epub.js 在线阅读。

**Architecture:** 后端将 `Book.content` (String TEXT) 改为 `Book.epubData` (byte[] MEDIUMBLOB)；上传端点改为 multipart/form-data 接收 EPUB 文件；新增 GET 端点返回 EPUB 二进制流。管理端移除 HTML 文本区改为 EPUB 文件上传。读者端新增 BookReader.vue 和 /book/:id/read 路由，使用 epub.js 渲染。

**Tech Stack:** Java 17 / Spring Boot 3 / JDK ZipInputStream (后端), Vue 3 / epub.js v0.3 (前端)

---

### Task 1: 后端 — Book 实体 content → epubData

**Files:**
- Modify: `library-server/src/main/java/com/library/entity/Book.java`

- [ ] **Step 1: 修改字段定义**

将第 32-34 行：
```java
@Lob
@Column
private String content;
```

改为：
```java
@Lob
@Column(columnDefinition = "MEDIUMBLOB")
private byte[] epubData;
```

- [ ] **Step 2: 修改全参构造函数**

第 47-61 行，将 `String content` 参数改为 `byte[] epubData`，对应赋值改为 `this.epubData = epubData;`

- [ ] **Step 3: 修改 getter/setter + Builder**

第 77 行 getter 改为 `public byte[] getEpubData()`，第 78 行 setter 改为 `public void setEpubData(byte[] epubData)`。

Builder 中第 107 行 `private String content` 改为 `private byte[] epubData`，第 119 行 `.content(String)` → `.epubData(byte[])`。

Builder.build() 第 124-125 行参数从 `content` 改为 `epubData`。

- [ ] **Step 4: Commit**

```bash
git add library-server/src/main/java/com/library/entity/Book.java
git commit -m "feat: change Book.content String to epubData byte[] MEDIUMBLOB"
```

---

### Task 2: 后端 — DTO 改动

**Files:**
- Modify: `library-server/src/main/java/com/library/dto/BookRequest.java`
- Modify: `library-server/src/main/java/com/library/dto/BookResponse.java`

- [ ] **Step 1: BookRequest 移除 content**

删除 `private String content;` 及其 getter `getContent()` 和 setter `setContent()`。

- [ ] **Step 2: BookResponse 移除 content，新增 hasEpub**

删除 `private String content;` 及其 getter/setter，以及 Builder 中的 `.content()` 方法。

新增字段：
```java
private boolean hasEpub;

public boolean isHasEpub() { return hasEpub; }
public void setHasEpub(boolean hasEpub) { this.hasEpub = hasEpub; }
```

Builder 新增：
```java
private boolean hasEpub;
public Builder hasEpub(boolean hasEpub) { this.hasEpub = hasEpub; return this; }
```

build() 中新增 `r.setHasEpub(hasEpub);`

- [ ] **Step 3: Commit**

```bash
git add library-server/src/main/java/com/library/dto/BookRequest.java library-server/src/main/java/com/library/dto/BookResponse.java
git commit -m "feat: remove content from DTOs, add hasEpub to BookResponse"
```

---

### Task 3: 后端 — BookService EPUB 上传 + 元数据提取 + 读取

**Files:**
- Modify: `library-server/src/main/java/com/library/service/BookService.java`

- [ ] **Step 1: 新增 EPUB 元数据提取方法**

在 BookService 末尾添加：

```java
private String extractEpubMetadata(byte[] epubData, String tagName) {
    try (java.util.zip.ZipInputStream zis = new java.util.zip.ZipInputStream(new java.io.ByteArrayInputStream(epubData))) {
        java.util.zip.ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            if (entry.getName().endsWith(".opf")) {
                StringBuilder sb = new StringBuilder();
                byte[] buf = new byte[4096];
                int len;
                while ((len = zis.read(buf)) > 0) {
                    sb.append(new String(buf, 0, len, java.nio.charset.StandardCharsets.UTF_8));
                }
                String xml = sb.toString();
                int start = xml.indexOf("<" + tagName);
                if (start >= 0) {
                    start = xml.indexOf(">", start) + 1;
                    int end = xml.indexOf("</" + tagName, start);
                    if (end >= 0) return xml.substring(start, end).trim();
                }
                break;
            }
        }
    } catch (Exception e) { /* ignore */ }
    return null;
}
```

- [ ] **Step 2: 改造 createBook 接收 MultipartFile**

替换现有 `createBook(BookRequest req)` 方法：

```java
public BookResponse createBook(MultipartFile file, BookRequest req) {
    String title = req.getTitle();
    String author = req.getAuthor();
    byte[] epubData = null;
    try {
        epubData = file.getBytes();
        if (title == null || title.isBlank()) {
            title = extractEpubMetadata(epubData, "dc:title");
        }
        if (author == null || author.isBlank()) {
            author = extractEpubMetadata(epubData, "dc:creator");
        }
    } catch (Exception e) {
        throw new RuntimeException("EPUB文件读取失败", e);
    }
    
    Book book = Book.builder()
        .title(title != null ? title : "未知书名")
        .author(author != null ? author : "未知作者")
        .isbn(req.getIsbn())
        .categoryId(req.getCategoryId())
        .coverUrl(req.getCoverUrl())
        .description(req.getDescription())
        .epubData(epubData)
        .status(1)
        .build();
    book = bookRepository.save(book);
    return toResponse(book);
}
```

新增 import：`import org.springframework.web.multipart.MultipartFile;`

- [ ] **Step 3: 改造 updateBook 接收可选 EPUB**

```java
public BookResponse updateBook(Long id, MultipartFile file, BookRequest req) {
    Book book = bookRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("图书不存在: " + id));
    book.setTitle(req.getTitle());
    book.setAuthor(req.getAuthor());
    book.setIsbn(req.getIsbn());
    book.setCategoryId(req.getCategoryId());
    book.setCoverUrl(req.getCoverUrl());
    book.setDescription(req.getDescription());
    if (file != null && !file.isEmpty()) {
        try {
            book.setEpubData(file.getBytes());
        } catch (Exception e) {
            throw new RuntimeException("EPUB文件读取失败", e);
        }
    }
    book = bookRepository.save(book);
    return toResponse(book);
}
```

- [ ] **Step 4: 新增 getEpubData 方法**

```java
public byte[] getEpubData(Long id) {
    Book book = bookRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("图书不存在: " + id));
    if (book.getEpubData() == null) {
        throw new EntityNotFoundException("该图书无EPUB内容");
    }
    return book.getEpubData();
}
```

- [ ] **Step 5: 更新 toResponse 方法**

将 `.content(book.getContent())` 改为 `.hasEpub(book.getEpubData() != null)`。

- [ ] **Step 6: Commit**

```bash
git add library-server/src/main/java/com/library/service/BookService.java
git commit -m "feat: EPUB upload with metadata extraction, getEpubData endpoint"
```

---

### Task 4: 后端 — AdminBookController 改为 Multipart + EPUB 读取端点

**Files:**
- Modify: `library-server/src/main/java/com/library/controller/AdminBookController.java`

- [ ] **Step 1: 读取当前 AdminBookController**

Read the file to understand existing endpoints.

- [ ] **Step 2: 改造 createBook 和 updateBook 端点**

`POST /api/admin/books`：
```java
@PostMapping
public ApiResponse<BookResponse> createBook(
    @RequestPart("file") MultipartFile file,
    @RequestPart(value = "title", required = false) String title,
    @RequestPart(value = "author", required = false) String author,
    @RequestPart("isbn") String isbn,
    @RequestPart(value = "categoryId", required = false) Long categoryId,
    @RequestPart(value = "coverUrl", required = false) String coverUrl,
    @RequestPart(value = "description", required = false) String description) {
    BookRequest req = new BookRequest();
    req.setTitle(title); req.setAuthor(author); req.setIsbn(isbn);
    req.setCategoryId(categoryId); req.setCoverUrl(coverUrl);
    req.setDescription(description);
    return ApiResponse.success(bookService.createBook(file, req));
}
```

`PUT /api/admin/books/{id}`：
```java
@PutMapping("/{id}")
public ApiResponse<BookResponse> updateBook(
    @PathVariable Long id,
    @RequestPart(value = "file", required = false) MultipartFile file,
    @RequestPart("title") String title,
    @RequestPart("author") String author,
    @RequestPart("isbn") String isbn,
    @RequestPart(value = "categoryId", required = false) Long categoryId,
    @RequestPart(value = "coverUrl", required = false) String coverUrl,
    @RequestPart(value = "description", required = false) String description) {
    BookRequest req = new BookRequest();
    req.setTitle(title); req.setAuthor(author); req.setIsbn(isbn);
    req.setCategoryId(categoryId); req.setCoverUrl(coverUrl);
    req.setDescription(description);
    return ApiResponse.success(bookService.updateBook(id, file, req));
}
```

新增 import：`import org.springframework.web.multipart.MultipartFile;`

- [ ] **Step 3: 新增 EPUB 读取端点**

```java
@GetMapping("/books/{id}/epub")
public ResponseEntity<byte[]> getEpub(@PathVariable Long id) {
    byte[] data = bookService.getEpubData(id);
    return ResponseEntity.ok()
        .contentType(org.springframework.http.MediaType.valueOf("application/epub+zip"))
        .body(data);
}
```

- [ ] **Step 4: Commit**

```bash
git add library-server/src/main/java/com/library/controller/AdminBookController.java
git commit -m "feat: multipart upload + GET epub endpoint in admin controller"
```

---

### Task 5: 后端 — BookController 公开 EPUB 读取 + application.yml

**Files:**
- Modify: `library-server/src/main/java/com/library/controller/BookController.java`
- Modify: `library-server/src/main/resources/application.yml`

- [ ] **Step 1: BookController 新增公开 EPUB 端点**

```java
@GetMapping("/books/{id}/epub")
public ResponseEntity<byte[]> getEpub(@PathVariable Long id) {
    byte[] data = bookService.getEpubData(id);
    return ResponseEntity.ok()
        .contentType(org.springframework.http.MediaType.valueOf("application/epub+zip"))
        .body(data);
}
```

- [ ] **Step 2: 调整 application.yml multipart 限制**

删除或注释掉 `max-file-size` 和 `max-request-size` 的限制：
```yaml
spring.servlet.multipart.max-file-size: -1
spring.servlet.multipart.max-request-size: -1
```

- [ ] **Step 3: 运行后端编译验证**

```bash
cd library-server && ./mvnw compile -q
```

- [ ] **Step 4: Commit**

```bash
git add library-server/src/main/java/com/library/controller/BookController.java library-server/src/main/resources/application.yml
git commit -m "feat: public EPUB endpoint + remove upload size limit"
```

---

### Task 6: 管理端 — BookForm.vue EPUB 上传 UI

**Files:**
- Modify: `admin-app/src/views/BookForm.vue`

- [ ] **Step 1: 读取当前文件**

Read the current BookForm.vue to understand the form layout and script.

- [ ] **Step 2: 替换 content 文本区为 EPUB 上传区**

在模板中，将图书内容 textarea（12 rows, label "支持HTML标签，待后续添加"）替换为：

```html
<div class="form-group">
  <label>EPUB 图书文件 <span class="required">*</span></label>
  <div 
    class="epub-upload-zone" 
    :class="{ 'has-file': epubFile }"
    @click="$refs.epubInput.click()"
    @dragover.prevent
    @drop.prevent="onDrop"
  >
    <input 
      ref="epubInput" 
      type="file" 
      accept=".epub" 
      style="display:none" 
      @change="onEpubSelected"
    />
    <template v-if="!epubFile">
      <span class="epub-upload-icon">+</span>
      <span>点击或拖拽上传 EPUB 文件</span>
    </template>
    <template v-else>
      <span class="epub-file-name">{{ epubFile.name }}</span>
      <span class="epub-file-size">({{ formatSize(epubFile.size) }})</span>
      <span class="epub-file-remove" @click.stop="epubFile = null">×</span>
    </template>
  </div>
</div>

<div class="form-group">
  <label>书名</label>
  <input v-model="form.title" placeholder="留空则从 EPUB 自动提取" />
</div>
<div class="form-group">
  <label>作者</label>
  <input v-model="form.author" placeholder="留空则从 EPUB 自动提取" />
</div>
```

- [ ] **Step 3: 添加 script 逻辑**

在 script setup 中添加：
```js
const epubFile = ref(null)

function onEpubSelected(e) {
  const file = e.target.files[0]
  if (file) epubFile.value = file
}

function onDrop(e) {
  const file = e.dataTransfer.files[0]
  if (file && file.name.endsWith('.epub')) epubFile.value = file
}

function formatSize(bytes) {
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}
```

- [ ] **Step 4: 修改提交逻辑使用 FormData**

修改 `save` 函数，构建 FormData：
```js
async function save() {
  if (!form.value.isbn.trim()) return alert('请输入ISBN')
  if (!isEdit.value && !epubFile.value) return alert('请上传EPUB文件')
  submitting.value = true
  try {
    const fd = new FormData()
    if (epubFile.value) fd.append('file', epubFile.value)
    fd.append('title', form.value.title || '')
    fd.append('author', form.value.author || '')
    fd.append('isbn', form.value.isbn)
    fd.append('categoryId', form.value.categoryId || '')
    fd.append('coverUrl', form.value.coverUrl || '')
    fd.append('description', form.value.description || '')
    
    if (isEdit.value) {
      await updateBook(route.params.id, fd)
    } else {
      await createBook(fd)
    }
    router.push('/books')
  } finally {
    submitting.value = false
  }
}
```

- [ ] **Step 5: 添加 EPUB 上传区样式**

在 scoped style 中添加：
```css
.epub-upload-zone {
  border: 2px dashed #d0c8b4;
  border-radius: 8px;
  padding: 32px;
  text-align: center;
  cursor: pointer;
  transition: border-color 0.2s;
  color: #a09880;
  font-size: 13px;
}
.epub-upload-zone:hover, .epub-upload-zone.has-file {
  border-color: #c9a96e;
  color: #c9a96e;
}
.epub-upload-icon {
  display: block;
  font-size: 28px;
  margin-bottom: 6px;
}
.epub-file-name { font-weight: 500; }
.epub-file-size { color: #a09880; margin-left: 8px; font-size: 12px; }
.epub-file-remove { margin-left: 12px; cursor: pointer; color: #c04040; }
.required { color: #c04040; }
```

- [ ] **Step 6: Commit**

```bash
git add admin-app/src/views/BookForm.vue
git commit -m "feat: EPUB upload UI replacing content textarea in BookForm"
```

---

### Task 7: 管理端 — api/book.js 适配 FormData

**Files:**
- Modify: `admin-app/src/api/book.js`

- [ ] **Step 1: 更新 createBook 和 updateBook**

```js
export function createBook(formData) {
  return api.post('/admin/books', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function updateBook(id, formData) {
  return api.put(`/admin/books/${id}`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
```

- [ ] **Step 2: 构建验证**

```bash
cd admin-app && npx vite build
```

- [ ] **Step 3: Commit**

```bash
git add admin-app/src/api/book.js
git commit -m "feat: FormData upload in admin book API"
```

---

### Task 8: 读者端 — 安装 epubjs + BookDetail 开始阅读按钮

**Files:**
- Modify: `reader-app/package.json`
- Modify: `reader-app/src/views/BookDetail.vue`

- [ ] **Step 1: 安装 epubjs**

```bash
cd reader-app && npm install epubjs@^0.3.93
```

- [ ] **Step 2: BookDetail.vue 新增「开始阅读」按钮**

在模板中（合适位置，如封面下方或元数据区），添加：
```html
<button
  v-if="book.hasEpub"
  class="btn-read"
  @click="$router.push(`/book/${book.id}/read`)"
>开始阅读</button>
```

样式：
```css
.btn-read {
  display: inline-block;
  padding: 10px 32px;
  background: var(--color-primary, #c9a96e);
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 15px;
  cursor: pointer;
  font-family: var(--font-serif);
  letter-spacing: 2px;
  transition: background 0.2s;
}
.btn-read:hover { background: var(--color-primary-hover, #b8944d); }
```

- [ ] **Step 3: Commit**

```bash
git add reader-app/package.json reader-app/package-lock.json reader-app/src/views/BookDetail.vue
git commit -m "feat: add epubjs dep + start reading button in BookDetail"
```

---

### Task 9: 读者端 — 新建 BookReader.vue 阅读器

**Files:**
- Create: `reader-app/src/views/BookReader.vue`

- [ ] **Step 1: 创建阅读器组件**

创建 `reader-app/src/views/BookReader.vue`：

```vue
<template>
  <div class="reader-page">
    <div class="reader-toolbar">
      <span class="toolbar-back" @click="$router.push(`/book/${bookId}`)">← 返回详情</span>
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
      <span>{{ currentPage }} / {{ totalPages }}</span>
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

onMounted(() => {
  book = ePub(`/api/books/${bookId}/epub`)
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
```

- [ ] **Step 2: Commit**

```bash
git add reader-app/src/views/BookReader.vue
git commit -m "feat: add BookReader with epub.js viewer"
```

---

### Task 10: 读者端 — Reader + 路由注册

**Files:**
- Modify: `reader-app/src/router/index.js`

- [ ] **Step 1: 路由注册**

在 routes 数组中添加：
```js
{ path: '/book/:id/read', name: 'book-reader', component: () => import('../views/BookReader.vue'), meta: { requiresAuth: true } }
```

- [ ] **Step 2: 构建验证**

```bash
cd reader-app && npx vite build
```

- [ ] **Step 3: Commit**

```bash
git add reader-app/src/router/index.js
git commit -m "feat: add /book/:id/read route for BookReader"
```

---

### Task 11: 全端构建验证

**Files:** 无

- [ ] **Step 1: 后端编译**

```bash
cd library-server && ./mvnw compile -q
```
预期：BUILD SUCCESS

- [ ] **Step 2: 管理端构建**

```bash
cd admin-app && npx vite build
```
预期：BUILD SUCCESS

- [ ] **Step 3: 读者端构建**

```bash
cd reader-app && npx vite build
```
预期：BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git commit -m "chore: all builds pass for EPUB feature" --allow-empty
```

---

## 测试清单

| 场景 | 预期 |
|---|---|
| 管理端上传有效 EPUB | 提取元数据，创建图书成功 |
| 管理端上传时填写书名/作者 | 覆盖 EPUB 内元数据 |
| 管理端编辑图书时不上传 EPUB | 保留原有 EPUB 数据 |
| 管理端上传非 EPUB 文件 | 前端 accept 属性阻止 |
| 读者端图书有 EPUB | 显示「开始阅读」按钮 |
| 读者端图书无 EPUB | 不显示按钮 |
| 读者端阅读器 | epub.js 渲染分页，章节导航，字号调节 |
| GET /api/books/{id}/epub | 返回二进制流，浏览器直接可用 |
| 数据库无 EPUB 的旧书 | hasEpub=false，阅读端点 404 |
