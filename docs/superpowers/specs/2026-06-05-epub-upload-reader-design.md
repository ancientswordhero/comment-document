# EPUB 图书上传与阅读 设计文档

**日期：** 2026-06-05  
**状态：** 已确认

---

## 概述

支持上传 EPUB 格式图书并在读者端使用 epub.js 进行在线阅读。

---

## 一、架构

```
管理端                          后端                          读者端
BookForm.vue                BookController              BookDetail.vue
  │                           │                           │
  ├─ 上传 EPUB ──────────→  POST /api/admin/books    BLOB → MySQL       │
  │   (multipart)             ├─ 解析 EPUB 元数据       │
  │                           └─ 存为 MEDIUMBLOB        │
  │                                                     │
                            GET /api/books/{id}/epub ─→ 返回 EPUB 二进制
                                                          │
                                                       BookReader.vue
                                                       epub.js 渲染阅读器
```

---

## 二、后端

### Book 实体

```java
// 原先：@Lob String content
// 改为：
@Lob
@Column(columnDefinition = "MEDIUMBLOB")
private byte[] epubData;
```

最大支持约 16MB（MEDIUMBLOB），不设上传大小限制。

### 上传端点

`POST /api/admin/books` 改为 `multipart/form-data`：

| 字段 | 类型 | 说明 |
|---|---|---|
| file | MultipartFile | EPUB 文件 |
| title | String | 可选，留空则从 EPUB 元数据提取 |
| author | String | 可选，留空则从 EPUB 元数据提取 |
| isbn | String | 必填 |
| categoryId | String | 分类 |
| coverUrl | String | 封面URL |
| description | String | 简介 |

后端从 EPUB 中提取 dc:title 和 dc:creator 作为默认书名和作者。

### 读取端点

`GET /api/books/{id}/epub` — 返回 `application/epub+zip` 二进制流。

### BookRequest / BookResponse

- `BookRequest` 移除 `content` 字段
- `BookResponse` 移除 `content` 字段，新增 `hasEpub` (boolean)，EPUB 数据本身不通过 JSON 返回

### 依赖

不引入重型 EPUB 库。使用 JDK 内置 `java.util.zip.ZipInputStream` 读取 EPUB（本质是 ZIP），用 JAXP 解析 `META-INF/container.xml` 和 OPF 文件的 title/creator 元数据。

---

## 三、管理端

### BookForm.vue

- 移除 HTML content 文本区
- 新增 EPUB 文件上传区（点击或拖拽，accept=".epub"）
- 上传后显示文件名和文件大小
- 书名/作者字段：可手动填写以覆盖 EPUB 内元数据
- 封面：优先使用 EPUB 内嵌封面，否则手动上传
- 提交时用 FormData 包装所有字段

### admin-app/src/api/book.js

- `createBook(data)` 改为发送 `FormData`（`Content-Type: multipart/form-data`）
- `updateBook(id, data)` 同上

---

## 四、读者端

### BookDetail.vue

- 当 `book.hasEpub === true` 时，显示主题色「开始阅读」按钮
- 点击跳转到 `/book/:id/read`

### BookReader.vue（新建）

布局：

```
┌──────────────────────────────────────────┐
│  ← 返回    章节选择 ▼    字号 A- A+      │  工具栏
├──────────────────────────────────────────┤
│                                          │
│          epub.js 渲染区域                  │  阅读区
│          (分页/滚动模式)                    │
│                                          │
├──────────────────────────────────────────┤
│        上一页  ◄── 1/342 ──►  下一页       │  页码栏
└──────────────────────────────────────────┘
```

- 从 `GET /api/books/{id}/epub` 加载 EPUB 文件
- epub.js 初始化：`new ePub("/api/books/{id}/epub")`
- 主题色跟随网站 `--color-primary: #c9a96e`
- 支持分页和滚动两种模式
- 章节下拉导航
- 字号调节（小/中/大）
- 背景米白色，文字深棕色

### 路由

`/book/:id/read` → `BookReader.vue`，`meta: { requiresAuth: true }`

### 依赖

`package.json` 新增 `epubjs` (`^0.3.93`)

---

## 五、数据迁移

现有 4 本种子书（红楼梦、三体、史记、论语）的 `content` 字段为空，无需数据迁移。`content` 列由 Hibernate `ddl-auto: update` 自动从 TEXT 改为 MEDIUMBLOB。

---

## 六、涉及文件

| 层 | 文件 | 改动 |
|---|---|---|
| 后端 | `Book.java` | `String content` → `byte[] epubData` |
| 后端 | `BookRequest.java` | 移除 `content` |
| 后端 | `BookResponse.java` | 移除 `content`，新增 `hasEpub` |
| 后端 | `BookService.java` | 上传用 MultipartFile，EPUB 元数据提取，新增 `getEpubData()` |
| 后端 | `AdminBookController.java` | 上传改 FormData，新增 `GET /api/admin/books/{id}/epub` |
| 后端 | `BookController.java` | 新增 `GET /api/books/{id}/epub` |
| 后端 | `application.yml` | 移除 `max-file-size` 上限 |
| 管理端 | `BookForm.vue` | 移除 content 文本区，新增 EPUB 上传 |
| 管理端 | `admin-app/src/api/book.js` | FormData 提交 |
| 读者端 | `BookDetail.vue` | 新增「开始阅读」按钮 |
| 读者端 | **新建** `BookReader.vue` | epub.js 阅读器 |
| 读者端 | `router/index.js` | 新增 `/book/:id/read` |
| 读者端 | `package.json` | 新增 `epubjs` |

---

## 七、不做

- 不引入重型 EPUB 解析库
- 不支持 PDF/txt 等其他格式
- 阅读器不做书签/笔记/高亮
- 不改变现有封面上传逻辑
- 不做阅读进度服务端同步（后续迭代）
