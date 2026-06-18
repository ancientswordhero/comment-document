# 登录页雨滴背景动效 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在登录页添加 Canvas 雨滴背景动效，雨线从上至下以书法笔触风格呈现

**Architecture:** 新增 `RainCanvas.vue` 组件，使用 Canvas + requestAnimationFrame 管理 45 条雨线的绘制与动画循环。组件通过 `ResizeObserver` 自适应窗口尺寸，通过 `visibilitychange` 在后台时暂停动画。在 `AuthForm.vue` 中引入，插入背景图和遮罩之间。

**Tech Stack:** Vue 3 (Composition API), Canvas 2D, requestAnimationFrame, ResizeObserver

---

## 文件变更

| 文件 | 操作 |
|------|------|
| `login-app/src/components/RainCanvas.vue` | 新增 |
| `login-app/src/components/AuthForm.vue` | 修改 — 引入 RainCanvas，调整 z-index |

---

### Task 1: 创建 RainCanvas.vue 组件

**Files:**
- Create: `login-app/src/components/RainCanvas.vue`

- [ ] **Step 1: 编写 RainCanvas.vue 完整代码**

```vue
<template>
  <canvas ref="canvasRef" class="rain-canvas"></canvas>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  count: { type: Number, default: 45 },
  color: { type: String, default: '74,61,47' },
  minSpeed: { type: Number, default: 0.3 },
  maxSpeed: { type: Number, default: 0.8 },
  minLength: { type: Number, default: 60 },
  maxLength: { type: Number, default: 200 }
})

const canvasRef = ref(null)
let ctx = null
let animId = null
let drops = []
let resizeObserver = null

function createDrop(canvasW, canvasH) {
  const length = props.minLength + Math.random() * (props.maxLength - props.minLength)
  return {
    x: Math.random() * canvasW,
    y: Math.random() * canvasH,
    speed: props.minSpeed + Math.random() * (props.maxSpeed - props.minSpeed),
    length,
    opacity: 0.15 + Math.random() * 0.30,
    thickness: 0.5 + Math.random() * 1.0
  }
}

function initDrops(canvasW, canvasH) {
  drops = []
  for (let i = 0; i < props.count; i++) {
    drops.push(createDrop(canvasW, canvasH))
  }
}

function draw() {
  if (!ctx) return
  const canvas = canvasRef.value
  if (!canvas) return
  const w = canvas.width
  const h = canvas.height

  ctx.clearRect(0, 0, w, h)

  for (const d of drops) {
    const { x, y, length, opacity, thickness } = d
    const [r, g, b] = props.color.split(',').map(v => parseInt(v.trim()))

    // 渐变：顶部淡 → 底部深
    const grad = ctx.createLinearGradient(x, y, x, y + length)
    grad.addColorStop(0, `rgba(${r},${g},${b},0)`)
    grad.addColorStop(0.2, `rgba(${r},${g},${b},${opacity * 0.3})`)
    grad.addColorStop(0.6, `rgba(${r},${g},${b},${opacity * 0.7})`)
    grad.addColorStop(1, `rgba(${r},${g},${b},${opacity})`)

    ctx.beginPath()
    ctx.moveTo(x, y)
    ctx.lineTo(x, y + length)
    ctx.strokeStyle = grad
    ctx.lineWidth = thickness
    ctx.lineCap = 'round'
    ctx.stroke()
  }
}

function update() {
  const canvas = canvasRef.value
  if (!canvas) return
  const h = canvas.height

  for (const d of drops) {
    d.y += d.speed
    if (d.y > h) {
      d.y = -d.length
      d.x = Math.random() * canvas.width
      d.speed = props.minSpeed + Math.random() * (props.maxSpeed - props.minSpeed)
      d.length = props.minLength + Math.random() * (props.maxLength - props.minLength)
      d.opacity = 0.15 + Math.random() * 0.30
      d.thickness = 0.5 + Math.random() * 1.0
    }
  }
}

function loop() {
  update()
  draw()
  animId = requestAnimationFrame(loop)
}

function handleResize(entries) {
  for (const entry of entries) {
    const { width, height } = entry.contentRect
    if (ctx && width > 0 && height > 0) {
      const canvas = canvasRef.value
      canvas.width = width
      canvas.height = height
      initDrops(width, height)
    }
  }
}

function handleVisibility() {
  if (document.hidden) {
    if (animId) {
      cancelAnimationFrame(animId)
      animId = null
    }
  } else {
    if (!animId) {
      animId = requestAnimationFrame(loop)
    }
  }
}

onMounted(() => {
  const canvas = canvasRef.value
  ctx = canvas.getContext('2d')

  canvas.width = canvas.offsetWidth
  canvas.height = canvas.offsetHeight
  initDrops(canvas.width, canvas.height)

  animId = requestAnimationFrame(loop)

  resizeObserver = new ResizeObserver(handleResize)
  resizeObserver.observe(canvas)

  document.addEventListener('visibilitychange', handleVisibility)
})

onUnmounted(() => {
  if (animId) cancelAnimationFrame(animId)
  if (resizeObserver) resizeObserver.disconnect()
  document.removeEventListener('visibilitychange', handleVisibility)
})
</script>

<style scoped>
.rain-canvas {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
  pointer-events: none;
}
</style>
```

- [ ] **Step 2: 提交 RainCanvas 组件**

```bash
git add login-app/src/components/RainCanvas.vue
git commit -m "feat: add RainCanvas component for login page rain effect"
```

---

### Task 2: 在 AuthForm.vue 中集成 RainCanvas

**Files:**
- Modify: `login-app/src/components/AuthForm.vue`

- [ ] **Step 1: 模板中插入 RainCanvas 组件**

在 `AuthForm.vue` 的 `<template>` 中，将 RainCanvas 插入到 `<img>` 背景图和 `.auth-overlay` 之间：

将第 2-4 行：

```html
  <div class="auth-wrapper">
    <img :src="bgSrc" class="auth-bg" alt="" />
    <div class="auth-overlay"></div>
```

修改为：

```html
  <div class="auth-wrapper">
    <img :src="bgSrc" class="auth-bg" alt="" />
    <RainCanvas />
    <div class="auth-overlay"></div>
```

- [ ] **Step 2: script 中导入 RainCanvas**

在 `<script setup>` 块顶部（`import bgSrc` 之前）添加导入：

```js
import RainCanvas from './RainCanvas.vue'
```

- [ ] **Step 3: 调整 .auth-overlay 的 z-index**

由于插入了 RainCanvas（z-index: 1），遮罩和卡片的 z-index 需要递增。

将 `.auth-overlay` 的 `z-index` 从 `1` 改为 `2`：

```css
.auth-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg,
    rgba(245,240,232,0.6) 0%,
    rgba(250,250,247,0.7) 50%,
    rgba(245,240,232,0.6) 100%
  );
  z-index: 2;
}
```

将 `.auth-card` 的 `z-index` 从 `2` 改为 `3`：

```css
.auth-card {
  position: relative;
  z-index: 3;
  /* ...其余不变... */
}
```

- [ ] **Step 4: 提交集成变更**

```bash
git add login-app/src/components/AuthForm.vue
git commit -m "feat: integrate RainCanvas into login page"
```

---

### Task 3: 验证效果

**Files:**
- 无需修改，仅验证

- [ ] **Step 1: 启动开发服务器**

```bash
cd login-app && npm run dev
```

- [ ] **Step 2: 浏览器打开 http://localhost:5173**

手动确认：
- 背景图上可见 45 条左右雨线垂直下落
- 每条雨线顶部淡底部深、顶部细底部粗
- 雨线速度、长度各有不同，层次自然
- 登录卡片和表单完全不受影响，可正常交互
- 缩放浏览器窗口，Canvas 自适应

- [ ] **Step 3: 验证通过后无需额外提交（无代码变更）**
