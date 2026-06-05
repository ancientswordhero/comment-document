# Token 过期自动退出 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 读者端 API 返回 401 时弹窗提示并自动清除登录状态，跳转到登录页。

**Architecture:** 仅修改 reader-app 的 axios 响应拦截器，在错误分支中新增 401 检测和登出逻辑。

**Tech Stack:** JavaScript / Axios

---

### Task 1: 读者端 axios 拦截器增加 401 处理

**Files:**
- Modify: `reader-app/src/api/index.js:23-27`

- [ ] **Step 1: 修改响应拦截器的错误分支**

当前代码（第 23-27 行）：

```js
error => {
    const msg = error.response?.data?.message || '网络错误'
    console.error(msg)
    return Promise.reject(error)
}
```

改为：

```js
error => {
    const msg = error.response?.data?.message || '网络错误'
    if (error.response?.status === 401) {
        alert(msg)
        localStorage.removeItem('token')
        localStorage.removeItem('username')
        window.location.href = 'http://localhost:5176'
        return Promise.reject(error)
    }
    console.error(msg)
    return Promise.reject(error)
}
```

- [ ] **Step 2: 构建验证**

```bash
cd reader-app && npx vite build
```
预期：BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add reader-app/src/api/index.js
git commit -m "feat: auto-logout and redirect on 401 response"
```

---

## 测试清单

| 场景 | 预期 |
|---|---|
| API 返回 401 | alert 弹出消息 → 用户点确定 → 清除 token/username → 跳转登录页 |
| API 返回非 401 错误 | 仅 console.error，不触发登出 |
| 正常请求 | 不受影响 |
