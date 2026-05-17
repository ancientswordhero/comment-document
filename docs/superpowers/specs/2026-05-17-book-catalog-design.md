# 图书目录系统 — 设计文档

## 概述

线上图书馆的图书目录子系统，包含读者浏览端和管理后台。读者可按分类浏览、关键词搜索、查看图书详情；管理员可对图书进行增删改查、上下架、封面上传。

## 技术栈

| 层 | 选型 |
|---|------|
| 后端 | Java Spring Boot（单体，经典三层） |
| 前端-读者端 | Vue 3（独立应用，端口 5173） |
| 前端-管理端 | Vue 3（独立应用，端口 5174） |
| 数据库 | MySQL |
| 封面存储 | 本地磁盘 `/uploads/covers/`，UUID 重命名 |

## 数据库设计

### categories（分类表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT AUTO_INCREMENT PK | 主键 |
| name | VARCHAR(50) NOT NULL | 分类名 |
| parent_id | BIGINT NULL | 父分类 ID，NULL 为顶级 |
| sort_order | INT DEFAULT 0 | 排序 |

### books（图书表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT AUTO_INCREMENT PK | 主键 |
| title | VARCHAR(200) NOT NULL | 书名 |
| author | VARCHAR(100) NOT NULL | 作者 |
| isbn | VARCHAR(20) UNIQUE NOT NULL | ISBN |
| category_id | BIGINT FK → categories.id | 分类 |
| cover_url | VARCHAR(500) NULL | 封面相对路径 |
| description | TEXT NULL | 简介（富文本 HTML） |
| status | TINYINT DEFAULT 1 | 0=下架，1=上架 |
| created_at | DATETIME DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

## API 设计

统一响应格式：`{ "code": 200, "message": "success", "data": {...} }`

统一分页格式：`{ "records": [...], "total": 100, "page": 1, "size": 20 }`

### 读者端

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/books` | 图书列表，支持 `keyword`、`categoryId`、`page`、`size` |
| GET | `/api/books/{id}` | 图书详情 |
| GET | `/api/categories` | 全部分类（树形结构） |

### 管理端

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/books` | 全部图书（含下架），同读者端筛选分页参数 |
| POST | `/api/admin/books` | 新增图书 |
| PUT | `/api/admin/books/{id}` | 编辑图书 |
| DELETE | `/api/admin/books/{id}` | 删除图书 |
| PUT | `/api/admin/books/{id}/status` | 上架/下架切换 |
| POST | `/api/admin/upload/cover` | 上传封面（multipart/form-data） |

## 后端项目结构

```
library-server/
├── controller/
│   ├── BookController.java        (读者端 API)
│   └── AdminBookController.java   (管理端 API)
├── service/
│   ├── BookService.java
│   └── FileService.java
├── repository/
│   ├── BookRepository.java
│   └── CategoryRepository.java
├── entity/
│   ├── Book.java
│   └── Category.java
├── dto/
│   ├── BookRequest.java
│   └── BookResponse.java
└── config/
    ├── WebConfig.java             (CORS)
    └── FileUploadConfig.java
```

## 前端设计

### 读者端

**视觉风格**：轻量新中式 — 暖灰白底色、浅米色轮播区、白色卡片 + 暖灰细线边框、淡金色点缀。标题用思源宋体，正文用思源黑体。整体留白多，元素克制。

**布局**（参考 B 站结构）：
- 顶部导航：Logo 左 + 搜索框居中 + 登录/注册右，白底浅阴影
- 轮播区：主轮播图 + 右侧 2 个推荐位，浅米色背景（Vue 动态轮播，初始静态占位）
- 左侧分类树：二级分类（父分类—子分类），选中态金色左边线
- 主区域：4 列图书卡片网格
- 底部分页

**路由**：

| 路由 | 页面 |
|------|------|
| `/` | 首页（图书列表 + 搜索 + 分类筛选 + 轮播） |
| `/book/:id` | 图书详情（封面 + 完整信息 + 富文本简介） |

**组件树**：

```
App.vue
├── AppHeader.vue          (Logo + 搜索栏 + 用户入口)
├── CategoryNav.vue        (左侧分类树)
└── <router-view>
    ├── BookList.vue
    │   ├── BookCarousel.vue  (轮播图)
    │   ├── SearchBar.vue     (搜索框 + 分类筛选)
    │   ├── BookCard.vue      (封面 + 书名 + 作者)
    │   └── Pagination.vue    (分页)
    └── BookDetail.vue
```

### 管理端

**视觉风格**：同读者端，轻量新中式。

**布局**：
- 顶部：标题 + 管理员入口，白底
- 操作栏：搜索、分类筛选、状态下拉 + 新增按钮（淡金）
- 数据表格：封面缩略图、书名、作者、ISBN、分类、状态标签、操作列（编辑/上下架/删除）
- 新增/编辑弹窗：书名、作者、ISBN、分类、封面上传区、简介（富文本）

**路由**：

| 路由 | 页面 |
|------|------|
| `/` | 图书列表 |
| `/book/new` | 新增图书表单 |
| `/book/:id/edit` | 编辑图书表单 |

**组件树**：

```
App.vue
├── AdminHeader.vue
└── <router-view>
    ├── BookTable.vue     (筛选栏 + 表格 + 分页)
    └── BookForm.vue      (新增/编辑表单 + 封面上传 + 富文本)
```

## 错误处理

- 后端统一异常处理（`@ControllerAdvice`），返回统一错误响应格式
- 前端 axios 拦截器统一处理网络错误和业务错误码
- 表单校验：前端基础校验（必填、ISBN 格式）+ 后端兜底校验

## 测试策略

- 后端：Service 层单元测试（JUnit + Mockito）、Controller 层集成测试（MockMvc）
- 前端：组件测试（Vitest + Vue Test Utils），覆盖关键交互路径
- 不对数据库和文件系统做 mock：集成测试使用 H2 内存库

## 不在当前范围

- 用户认证和授权（后续用户系统实现）
- 借阅功能（后续借阅管理实现）
- 评论/评分
- 批量导入导出
- 全文搜索（MySQL LIKE 起步，后续可迁移至 Elasticsearch）
