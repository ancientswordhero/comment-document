# Bilibili 风格视差 Header 设计

改造 reader-app 的 BannerHeader.vue，使用用户提供的 32 张 B 站主题横幅素材 (3840×360 webp)，实现鼠标驱动的横向视差效果。

## 视差效果

- 监听 `banner` 区域的 `mousemove`
- 鼠标在左侧 → 背景向右偏移，展示图片左侧内容；鼠标在右侧 → 背景向左偏移
- 偏移范围 ±60px，映射自鼠标在 banner 内的 x 方向位置比例 (0~1)
- `requestAnimationFrame` 循环 + lerp 缓动 (`current += (target - current) * 0.08`)，实现平滑跟随
- 移动端 (触摸设备) 降级为静态 `object-fit: cover`，不启用视差

## 组件结构

保持现有 DOM 骨架，仅在 `banner-bg-wrap > img` 上应用 translateX：

```
banner-wrapper
├── banner-nav          (固定不动)
│   ├── nav-left        (Logo)
│   ├── nav-links       (导航链接)
│   └── nav-right       (用户操作/登录)
└── banner              (mousemove 监听区域)
    ├── banner-bg-wrap  (视差层)
    │   └── img         (3840×360 webp, transform: translateX(...))
    ├── banner-mask     (渐变遮罩, 固定)
    └── banner-center   (Logo+搜索+tagline, 固定)
```

移除现有 `banner-character-wrap`（角色装饰），因为 webp 横幅本身已包含角色插画。

## 图片管理

- 32 张 webp 存入 `reader-app/src/assets/header/`
- 组件挂载时随机选 1 张加载
- 可选：后续支持定时轮换 (此 spec 暂不包含定时切换)
- 避免预加载的复杂性：随机抽 1 张直接渲染，img 标签自带 loading 行为

## 保留不变

- 导航栏所有功能 (logo、首页/分类浏览/最新上架 链接)
- 右侧登录/未登录状态逻辑 (用户信息、书架、收件箱、退出、注销)
- 搜索框及 enter/按钮触发 `setSearchKeyword`
- 渐变遮罩 (`banner-mask`)
- UserProfileDialog
- 响应式断点 (1024/768/480px)

## 移除内容

- `bannerBg` (旧 banner-bg.jpg)
- `characterSrc` (旧 banner-character.png)
- `banner-character-wrap` 及其样式

## 文件变更

| 文件 | 操作 |
|---|---|
| `reader-app/src/components/BannerHeader.vue` | 重写 script 和 style 的 banner 相关部分 |
| `reader-app/src/assets/header/*.webp` | 新增 32 张素材 |

## 测试要点

- 桌面端鼠标在 banner 上左右移动，背景平滑跟随 (无卡顿)
- 鼠标移出 banner 区域，背景回到居中位置
- 移动端 (Chrome DevTools 模拟) 背景静态居中，无抖动
- 搜索框输入 + enter / 点击按钮触发搜索
- 登录/未登录状态切换正常
- 响应式断点下导航栏和搜索框正常显示
