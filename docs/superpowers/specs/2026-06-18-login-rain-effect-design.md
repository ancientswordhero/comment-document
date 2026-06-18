# 登录页雨滴背景动效设计

## 概述

为登录页面添加纯装饰性 Canvas 雨滴背景动效，雨线从上至下以书法笔触风格呈现，与"书海"品牌的东方气质统一。

## 技术选型

采用 Canvas + requestAnimationFrame 方案，理由：
- 40-50 条并发雨线，Canvas 单帧绘制性能远超 DOM 动画
- 笔触质感（渐变、粗细变化）在 Canvas 上易于实现
- 独立于 Vue 组件树，不增加响应式开销

## 分层结构

从底到顶：
```
.auth-bg（静态背景图，z=0）
  → RainCanvas（雨滴 Canvas，z=1）
  → .auth-overlay（半透明遮罩，z=1 → 改为 z=2）
  → .auth-card（登录卡片，z=2 → 改为 z=3）
```

## 组件设计

新增 `RainCanvas.vue`，在 `AuthForm.vue` 中引入，置于背景图和遮罩之间。

### Props

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `count` | Number | 45 | 同时可见的雨线数量 |
| `color` | String | `'74,61,47'` | 雨线 RGB 色值（对应 --color-text） |
| `minSpeed` | Number | 0.3 | 最慢速度（像素/帧） |
| `maxSpeed` | Number | 0.8 | 最快速度（像素/帧） |
| `minLength` | Number | 60 | 最短雨线（px） |
| `maxLength` | Number | 200 | 最长雨线（px） |

### 雨线数据结构

每条雨线：
```ts
{
  x: number          // 水平位置（随机，窗口宽度内均匀分布）
  y: number          // 当前垂直位置（顶端）
  speed: number      // 下落速度
  length: number     // 雨线长度
  opacity: number    // 整体透明度（0.15~0.45，模拟墨色浓淡）
  thickness: number  // 底部线宽（0.5~1.5px）
}
```

## 绘制规则（书法笔触）

每条雨线的渐变方向：**顶部最细最淡 → 向下逐渐变粗变深**。

使用 Canvas `createLinearGradient`：
- 顶端 `(x, y)`：透明度 0，线宽取 `thickness` 的 0.2 倍
- 底端 `(x, y + length)`：透明度取 `opacity`，线宽取 `thickness`

模拟毛笔"悬针竖"的收笔，也符合雨滴加速下坠的自然感。

颜色使用 `--color-text` (#4a3d2f) 的 RGB 分量，不过度饱和，保持水墨淡雅的调性。

## 动画循环

```
初始化 N 条雨线，随机分布在窗口中
每一帧：
  1. clearRect 清空 Canvas
  2. 绘制所有雨线
  3. 每条雨线 y += speed
  4. 当雨线顶端 y 超出窗口底部时，重置到顶部以上随机位置
```

Canvas 尺寸通过 `ResizeObserver` 响应窗口变化。

## 性能考量

- Canvas 绑定 `will-change: transform` 避免不必要的合成层重绘
- 组件卸载时取消 `requestAnimationFrame`
- 窗口不可见时（`visibilitychange`）暂停动画

## 影响范围

| 文件 | 操作 |
|------|------|
| `login-app/src/components/RainCanvas.vue` | 新增 |
| `login-app/src/components/AuthForm.vue` | 修改 — 引入 RainCanvas，调整 z-index |

## 验收标准

- 登录页可见 40-50 条雨线沿垂直方向下落
- 每条雨线顶部淡底部深、顶部细底部粗
- 雨线速度、长度、透明度各有随机变化，呈现自然层次
- 不影响表单交互，不遮挡卡片内容
- 窗口缩放时 Canvas 自适应
