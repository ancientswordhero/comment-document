# 数据存储重构 — 设计文档

## 概述

将项目所有媒体数据统一整理。EPUB 和封面图片均以数据库为主存储、`shuhai/` 文件夹为备份副本。消除封面图片散落文件系统的现状。

## 存储策略

| 数据类型 | 主存储 | 备份 |
|---------|--------|------|
| EPUB | MySQL `books.epub_data` MEDIUMBLOB（不变） | `shuhai/epub/{bookId}.epub` |
| 封面 | MySQL `books.cover_data` MEDIUMBLOB（新增） | `shuhai/covers/{bookId}.jpg` |

## 目录结构

```
shuhai/
├── .gitignore        ← 忽略所有二进制文件
├── epub/
│   ├── README.txt    ← "EPUB电子书备份副本，以bookId命名，来源为books.epub_data"
│   ├── 1.epub
│   └── 2.epub
└── covers/
    ├── README.txt    ← "图书封面备份副本，以bookId命名，来源为books.cover_data"
    ├── 1.jpg
    └── 2.jpg
```

## 数据库变更

**books 表：**

| 操作 | 字段 | 类型 | 说明 |
|------|------|------|------|
| 新增 | `cover_data` | MEDIUMBLOB | 封面图片二进制 |
| 删除 | `cover_url` | VARCHAR(500) | 迁移完成后删除 |

`epub_data` 字段保持不变。

## API 变更

| 变更 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 新增 | GET | `/api/books/{id}/cover` | 返回封面图片二进制流（Content-Type: image/jpeg） |
| 删除 | - | `/uploads/covers/**` | 不再提供封面文件直链 |
| 不变 | GET | `/api/books/{id}/epub` | EPUB 读取接口不变 |

## 代码变更

### 后端

- **Book.java** — 新增 `coverData` 字段，删除 `coverUrl`
- **BookService.java** — `createBook()`/`updateBook()` 中封面改为存 `coverData`，同时写备份到 `shuhai/covers/`；`getEpubData()` 不变；新增 `getCoverData()` 方法；创建/更新 EPUB 时同步写 `shuhai/epub/` 备份
- **FileService.java** — 不再需要，删除
- **FileUploadConfig.java** — 不再需要，删除
- **BookController.java** — 新增 `GET /api/books/{id}/cover` 端点
- **AdminBookController.java** — 封面上传适配（MultipartFile → byte[] 存入 coverData）
- **BookResponse.java** — `coverUrl` 改为 `hasCover`（Boolean）
- **WebConfig.java** — 移除 `/uploads/covers/**` 静态资源映射
- **数据迁移脚本** — 一次性将现有 `epub_data` 和 `cover_url` 对应的封面文件落盘到 `shuhai/`；将现有封面文件内容读入 `cover_data`

### 前端（reader-app & admin-app）

- **BookCard.vue / BookDetail.vue / AdminForm.vue / BookForm.vue** — 封面 `img src` 改为 `/api/books/{id}/cover` 接口
- **BookResponse 相关** — `coverUrl` 字段改为判断 `hasCover` 决定是否渲染封面

## 迁移步骤

1. 新增 `cover_data` 字段（DDL）
2. 迁移脚本：现有封面文件 → `cover_data`，同时落盘 `shuhai/covers/`
3. 迁移脚本：现有 `epub_data` → 落盘 `shuhai/epub/`
4. 部署新代码
5. 验证无误后删除 `cover_url` 字段和 `uploads/` 目录

## 边界与风险

- 封面格式：前端上传时校验为 `image/jpeg` 或 `image/png`，存储时统一为 JPEG
- 备份失败不影响主存储：`shuhai/` 写入失败只 log 不抛异常
- `shuhai/` 目录加入 `.gitignore`，二进制文件不入版本库
