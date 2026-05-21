# 举报与审核系统 — 设计文档

## 概述

为书评系统新增举报与审核功能。用户可举报不当书评，管理员在后台审核处理（删除或驳回），举报人和被举报人均在收件箱收到处理结果通知。

## 数据库设计

### reports（举报表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT AUTO_INCREMENT PK | 主键 |
| review_id | BIGINT FK → reviews.id ON DELETE CASCADE | 被举报书评 |
| reporter_id | BIGINT FK → users.id | 举报人 |
| reason | VARCHAR(20) NOT NULL | 预设：spam / abuse / fake / violation / other |
| detail | VARCHAR(200) NULL | reason=other 时的补充说明 |
| status | VARCHAR(20) DEFAULT 'pending' | pending / deleted / dismissed |
| admin_id | BIGINT FK → users.id NULL | 处理的管理员 |
| admin_note | VARCHAR(200) NULL | 管理员处理备注 |
| created_at | DATETIME DEFAULT CURRENT_TIMESTAMP | 举报时间 |
| resolved_at | DATETIME NULL | 处理时间 |

UNIQUE(review_id, reporter_id) — 同一用户对同一条书评只能举报一次

### notifications（通知表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT AUTO_INCREMENT PK | 主键 |
| user_id | BIGINT FK → users.id | 接收人（举报人或被举报人） |
| type | VARCHAR(20) NOT NULL | 通知类型，当前仅 'report_result' |
| title | VARCHAR(100) NOT NULL | 通知标题 |
| content | TEXT NOT NULL | 正文（含书评内容摘要、举报理由、处理结果） |
| is_read | TINYINT DEFAULT 0 | 0=未读 1=已读 |
| created_at | DATETIME DEFAULT CURRENT_TIMESTAMP | 通知时间 |

## API 设计

### 读者端

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/api/reviews/{id}/report` | 举报书评 `{ reason, detail? }` | JWT |
| GET | `/api/notifications` | 通知列表，`?page=&size=` | JWT |
| GET | `/api/notifications/unread-count` | 未读通知数量 `{ count }` | JWT |
| PUT | `/api/notifications/{id}/read` | 标记单条已读 | JWT |
| PUT | `/api/notifications/read-all` | 全部已读 | JWT |

### 管理端

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/api/admin/reports` | 举报列表 `?status=&page=&size=` | ADMIN |
| PUT | `/api/admin/reports/{id}/resolve` | 处理举报 `{ action, note? }` | ADMIN |

### 处理逻辑

**action = "delete"：**
1. 删除对应的书评（reviewRepository.delete）
2. report.status → 'deleted'，记录 admin_id 和 admin_note、resolved_at
3. 给被举报人发通知：你的书评"xxx..."因「{reason}」被举报，管理员已删除该评论
4. 给举报人发通知：你举报的书评"xxx..."已被管理员删除处理

**action = "dismiss"：**
1. report.status → 'dismissed'，记录 admin_id 和 admin_note、resolved_at
2. 给被举报人发通知：你的书评"xxx..."被举报「{reason}」，管理员审核后驳回举报
3. 给举报人发通知：你举报的书评"xxx..."管理员审核后驳回

### JWT Filter 扩展

```java
boolean needsAuth = path.equals("/api/auth/me")
    || path.startsWith("/api/bookshelf")
    || (path.startsWith("/api/reviews") && !"GET".equalsIgnoreCase(req.getMethod()))
    || (path.matches("/api/books/\\d+/reviews") && !"GET".equalsIgnoreCase(req.getMethod()))
    || path.startsWith("/api/notifications");
```

所有通知端点需 JWT 认证。

## 后端项目结构

```
library-server/
├── entity/
│   ├── Report.java              (新增)
│   └── Notification.java        (新增)
├── repository/
│   ├── ReportRepository.java    (新增)
│   └── NotificationRepository.java (新增)
├── dto/
│   ├── ReportRequest.java       (新增)
│   ├── ReportResponse.java      (新增)
│   ├── ResolveReportRequest.java(新增)
│   └── NotificationResponse.java(新增)
├── service/
│   ├── ReportService.java       (新增)
│   └── NotificationService.java (新增)
├── controller/
│   ├── ReportController.java    (新增，读者端)
│   ├── NotificationController.java (新增，读者端)
│   └── AdminReportController.java  (新增，管理端)
```

## 前端设计

### 读者端

**新增文件：**

| 文件 | 说明 |
|------|------|
| `src/api/report.js` | 举报 API + 通知 API |
| `src/components/ReportDialog.vue` | 举报弹窗（下拉选理由 + 条件文本框） |
| `src/views/Inbox.vue` | 收件箱页面（通知列表 + 已读/未读标记） |

**修改文件：**

| 文件 | 改动 |
|------|------|
| `src/router/index.js` | 新增 `/inbox` 路由 |
| `src/components/AppHeader.vue` | 用户名旁加收件箱入口 + 未读红点 |
| `src/components/ReviewItem.vue` | 操作栏加「举报」链接 |

### 管理端

**新增文件：**

| 文件 | 说明 |
|------|------|
| `src/api/report.js` | 举报管理 API 模块 |
| `src/views/ReportManagement.vue` | 举报列表 + 处理操作 |

**修改文件：**

| 文件 | 改动 |
|------|------|
| `src/router/index.js` | 新增 `/reports` 路由 |
| `src/components/AdminHeader.vue` | 导航加「举报管理」链接 |

### 举报预设选项

| 值 | 标签 | 是否需要补充说明 |
|----|------|-----------------|
| spam | 垃圾广告 | 否 |
| abuse | 人身攻击 | 否 |
| fake | 虚假信息 | 否 |
| violation | 违规内容 | 否 |
| other | 其他 | 是（必填，≤200字） |

### 交互规则

- 同一用户对同一条书评只能举报一次，重复举报返回 409
- 举报后评论保持可见，管理员处理后按需删除
- 用户不能举报自己的书评（前端隐藏 + 后端校验）
- 收件箱按时间倒序，未读通知左侧有金色小圆点
- AppHeader 收件箱入口旁显示未读数字红点

## 不在当前范围

- 用户私信/聊天
- 邮件通知
- 举报申诉
- 批量处理举报
- 举报统计/日志
