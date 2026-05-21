# 书评系统 — 设计文档

## 概述

为线上图书馆新增书评功能（参考 B 站评论系统）。用户可在图书详情页下方查看、发表、回复、点赞书评。支持按时间和热度排序，分页展示。纯文字评论，无评分，无头像。

## 数据库设计

### reviews（书评表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT AUTO_INCREMENT PK | 主键 |
| book_id | BIGINT FK → books.id ON DELETE CASCADE | 所属图书 |
| user_id | BIGINT FK → users.id | 发表用户 |
| parent_id | BIGINT FK → reviews.id NULL | NULL=顶级书评，非NULL=回复 |
| root_id | BIGINT FK → reviews.id NULL | 顶级书评ID（回复链的根），顶级书评为NULL |
| content | TEXT NOT NULL | 书评内容，纯文本，上限1000字 |
| like_count | INT DEFAULT 0 | 点赞数（冗余） |
| reply_count | INT DEFAULT 0 | 回复数（冗余，仅顶级书评维护） |
| created_at | DATETIME DEFAULT CURRENT_TIMESTAMP | 发表时间 |
| updated_at | DATETIME ON UPDATE CURRENT_TIMESTAMP | 编辑时间 |

- `parent_id` 实现一层嵌套：回复时 parent_id 指向直接父评论
- `root_id` 冗余字段：快速查询同属一条顶级书评的所有回复
- `like_count` / `reply_count` 冗余：避免列表查询时 COUNT 子查询
- `updated_at` 用于校验3分钟编辑窗口：`now - created_at < 3min`
- 级联删除：删图书 → 删所有书评；删顶级书评 → 级联删所有回复

### review_likes（点赞记录表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT AUTO_INCREMENT PK | 主键 |
| review_id | BIGINT FK → reviews.id ON DELETE CASCADE | 被点赞的书评 |
| user_id | BIGINT FK → users.id | 点赞用户 |

- UNIQUE(review_id, user_id) 确保一人只能点赞一次

## API 设计

统一响应格式（复用 `ApiResponse<T>`）。

### 读者端

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/api/books/{id}/reviews` | 获取书评列表（分页page/size，sort=time\|hot） | 无 |
| POST | `/api/books/{id}/reviews` | 发表顶级书评 `{ content }` | JWT |
| POST | `/api/reviews/{id}/reply` | 回复书评 `{ content }` | JWT |
| PUT | `/api/reviews/{id}` | 编辑书评内容（3分钟内） | JWT（本人） |
| DELETE | `/api/reviews/{id}` | 删除书评/回复 | JWT（本人或管理员） |
| POST | `/api/reviews/{id}/like` | 点赞 toggle（已赞则取消） | JWT |

### 管理端

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| DELETE | `/api/admin/reviews/{id}` | 管理员删除任意书评 | ADMIN |

### 排序规则

| 排序 | 顶级书评 | 回复 |
|------|---------|------|
| time | `created_at DESC` | `created_at ASC` |
| hot | `(like_count + reply_count) DESC, created_at DESC` | `created_at ASC` |

回复始终按时间正序，保持对话连贯。

### 响应格式

顶级书评列表项：
```json
{
  "id": 1, "bookId": 10, "username": "读者小明", "content": "...",
  "likeCount": 28, "replyCount": 3, "liked": true,
  "createdAt": "2026-05-20T10:30:00", "updatedAt": null,
  "replies": [
    { "id": 2, "parentId": 1, "username": "三体迷", "content": "...",
      "likeCount": 12, "liked": false,
      "createdAt": "2026-05-20T14:00:00" }
  ]
}
```

## JWT Filter 改动

`JwtFilter.java` 的 `needsAuth` 条件扩展为：

```java
boolean needsAuth = path.equals("/api/auth/me")
    || path.startsWith("/api/bookshelf")
    || (path.startsWith("/api/reviews") && !"GET".equalsIgnoreCase(req.getMethod()));
```

- GET 请求公开访问
- POST/PUT/DELETE 需认证（编辑/删除操作在 Service 层进一步校验身份）

## 后端项目结构

```
library-server/
├── entity/
│   └── Review.java              (新增)
├── repository/
│   ├── ReviewRepository.java    (新增)
│   └── ReviewLikeRepository.java(新增)
├── service/
│   └── ReviewService.java       (新增)
├── controller/
│   └── ReviewController.java    (新增，读者端)
├── dto/
│   ├── ReviewRequest.java       (新增)
│   └── ReviewResponse.java      (新增，含嵌套 replies)
```

## 前端设计

### 组件

`BookDetail.vue` 下方新增 `<ReviewSection>` 组件：

```
BookDetail.vue
├── detail-card (现有)
└── ReviewSection.vue  ← 新增
    ├── 发表框 (textarea + 发表按钮)
    ├── 排序标签 (按时间 / 按热度)
    ├── 书评列表
    │   ├── ReviewItem.vue (顶级书评)
    │   │   └── ReplyItem.vue (回复，嵌套在父评论下方)
    │   └── ...
    └── 分页 (上一页 / 下一页)
```

### 新增文件

- `reader-app/src/api/review.js` — 书评 API 模块
- `reader-app/src/components/ReviewSection.vue` — 书评区主组件
- `reader-app/src/components/ReviewItem.vue` — 单条书评/回复组件

### 交互规则

- **发表**：登录后显示输入框和发表按钮；未登录显示「登录后发表书评」提示
- **回复**：点击「回复」展开小输入框，回复后收起
- **编辑**：自己的书评/回复，`createdAt` 在3分钟内显示「编辑」链接，编辑时输入框替换文字
- **删除**：自己的显示「删除」；点击弹出确认对话框
- **点赞**：已登录可点击「赞」，toggle 模式，已赞状态高亮（金色加粗），未赞状态灰色
- **排序**：点击切换标签，重新加载第一页
- **分页**：每页10条顶级书评，回复默认全部展示不翻页
- **「(我)」标记**：当前用户自己的书评/回复用户名列旁显示

### 按钮样式

- 发表按钮、分页按钮：页面背景色 `#fafaf7` + 细边框 `#e0dbd0` + 灰褐色文字 `#8b8070`

## 错误处理

- 书评内容为空 → 400
- 内容超1000字 → 400
- 未登录发表/回复/点赞 → 401
- 非本人编辑/删除 → 403
- 超过3分钟编辑 → 403 "编辑时间已过"
- 图书不存在 → 404
- 书评不存在 → 404
- 重复点赞 → 幂等（toggle 模式自动处理）

## 不在当前范围

- 头像上传和展示
- 星级评分
- 富文本编辑器
- 书评举报功能
- @提醒功能
- 搜索书评
