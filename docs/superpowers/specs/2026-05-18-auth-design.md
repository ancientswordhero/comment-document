# 用户认证/登录系统 — 设计文档

## 概述

为线上图书馆新增用户认证系统。支持用户名+密码注册/登录，角色分为读者（READER）和管理员（ADMIN）。读者可自行注册，管理员由已有管理员预创建。统一登录页（独立应用），登录后根据角色自动跳转到读者端或管理端。采用 JWT 无状态认证。

## 技术决策

- **JWT（jjwt 库）**：无状态，适合前后端分离多应用架构
- **BCrypt 加密**：密码存储用 BCrypt
- **统一登录页**：独立 Vue 3 应用，端口 5175
- **URL 传 token**：登录后通过 query string 将 token 传给目标应用，目标应用提取后清除 URL

## 数据库设计

### users（用户表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT AUTO_INCREMENT PK | 主键 |
| username | VARCHAR(50) UNIQUE NOT NULL | 用户名 |
| password | VARCHAR(255) NOT NULL | BCrypt 哈希 |
| role | VARCHAR(20) NOT NULL | `READER` 或 `ADMIN` |
| created_at | DATETIME DEFAULT CURRENT_TIMESTAMP | 注册时间 |

预置数据：默认管理员 `admin`，密码 `admin123`，BCrypt 哈希在实现时用 `BCryptPasswordEncoder` 生成后写入 `data.sql`。

```sql
-- 密码为 admin123，BCrypt 编码（实现时生成实际值替换此占位）
INSERT IGNORE INTO users (username, password, role) VALUES
('admin', '<BCRYPT_HASH_OF_admin123>', 'ADMIN');
```

## API 设计

统一响应格式（复用现有 `ApiResponse<T>`）：

```
{ "code": 200, "message": "success", "data": {...} }
```

### Auth API

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/api/auth/register` | 读者注册 `{ username, password }` | 无 |
| POST | `/api/auth/login` | 登录，返回 `{ token, username, role }` | 无 |
| GET | `/api/auth/me` | 当前用户信息 `{ username, role }` | 需 JWT |
| POST | `/api/auth/admin` | 管理员新增管理员 `{ username, password }` | ADMIN |

### 认证机制

- **JWT payload**：`{ sub: userId, username, role, iat, exp }`，过期 24 小时
- **受保护路径**：
  - `/api/admin/**` → 需 ADMIN 角色
  - `/api/auth/me` → 需有效 JWT
  - 其余 API 公开访问
- **JWT Filter**：拦截受保护路径，解析 token，验证签名和过期，设置 SecurityContext
- **CORS 更新**：`WebConfig.java` 需添加 `http://localhost:5175` 到允许源列表

## 后端项目结构

```
library-server/
├── entity/
│   └── User.java              (新增)
├── repository/
│   └── UserRepository.java    (新增)
├── service/
│   └── UserService.java       (新增)
├── config/
│   ├── SecurityConfig.java    (新增，JWT filter 注册)
│   └── JwtUtil.java           (新增，token 生成/验证)
├── controller/
│   └── AuthController.java    (新增)
└── dto/
    ├── LoginRequest.java      (新增)
    ├── RegisterRequest.java   (新增)
    └── LoginResponse.java     (新增)
```

## 前端设计

### login-app（端口 5175）

沿用新中式风格（CSS 变量同 reader-app）。

**布局**：居中卡片，含 Logo + Tab 栏（登录/注册）+ 表单。

**登录表单**：用户名 + 密码 + 登录按钮。成功后跳转 → 读者到 `:5173`，管理员到 `:5174`，通过 `?token=xxx` 传递。

**注册表单**：用户名 + 密码 + 确认密码 + 注册按钮。成功后自动跳转读者端。

**组件树**：
```
App.vue
└── AuthForm.vue
```

### reader-app 改动

- **AppHeader.vue**：登录/注册链接改为显示用户名和退出按钮
- **路由守卫**：从 URL 提取 token → 调 `/api/auth/me` 验证 → 存入 localStorage → 清除 URL token；后续路由从 localStorage 读取；token 过期则回到登录页

### admin-app 改动

- **路由守卫**：同上（从 URL 提取 token 的逻辑），但额外检查 role 是否为 ADMIN
- **新增管理员页**：在路由 `/admins` 添加表单（用户名+密码+创建按钮），仅管理员可见

## 登录流程

```
用户 → login-app (:5175)
  │
  ├─ 输入用户名密码 → POST /api/auth/login
  │     │
  │     └─ 返回 { token, role: "READER" } → 跳转 :5173/?token=xxx
  │     或 { token, role: "ADMIN" } → 跳转 :5174/?token=xxx
  │
  └─ 注册 → POST /api/auth/register → 自动登录 → 跳转 :5173/?token=xxx
```

目标应用（reader-app / admin-app）启动时：
1. 检查 URL 是否有 `?token=xxx`
2. 有 → 调 `/api/auth/me` 验证 → 存 localStorage → `router.replace({ query: {} })` 清除 URL
3. 无 → 检查 localStorage 是否有 token → 验证 → 过期则跳回登录页

## 错误处理

- 用户名已存在 → 409
- 用户名或密码错误 → 401
- token 过期/无效 → 401
- 无权限（读者访问管理端 API）→ 403
- 表单校验（前端 + 后端 `@Valid`）→ 400

## 测试策略

- **后端单元测试**：UserService（注册、登录、token 验证）
- **后端集成测试**：AuthController（登录/注册/me 端点，MockMvc）
- **前端**：AuthForm 组件测试（表单校验、提交逻辑），路由守卫测试

## 不在当前范围

- token 刷新（refresh token）
- 记住我（长期有效 token）
- 密码修改、找回
- OAuth / 第三方登录
- 用户头像、个人资料
- 读者端"我的借阅"等需要认证的功能（仅 UI 入口预留）
