# 读者端 Token 过期自动退出 设计文档

**日期：** 2026-06-05  
**状态：** 已确认

---

## 概述

读者端当前未处理 401 响应，token 过期时用户无感知。改为检测到 401 后弹窗提示并自动清除登录状态。

---

## 改动

**单个文件：** `reader-app/src/api/index.js`

**响应拦截器错误分支** 当前逻辑（第 23-27 行）：

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

**行为：**
1. 捕获 401 状态码
2. `alert()` 弹出后端消息（如"登录已过期，请重新登录"），纯文本无 emoji
3. 用户点确定后，清除 token 和 username
4. 跳转到登录页 `http://localhost:5176`

---

## 不做

- 不修改后端 JwtFilter 或错误消息
- 不修改管理端（已有类似逻辑）
- 不修改登录端
- 不新增 toast 组件或其他 UI
